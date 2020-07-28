package fr.inria.anhalytics.annotate;

import com.mongodb.DBObject;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.annotate.exceptions.UnreachableAnnotateServiceException;
import fr.inria.anhalytics.annotate.exceptions.AnnotatorNotAvailableException;
import fr.inria.anhalytics.annotate.services.AnnotateService;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.data.Processings;
import fr.inria.anhalytics.commons.managers.MongoFileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Handles threads used to annotate the tei collection from MongoDB.
 *
 * @author Patrice, Achraf
 */
public class Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Annotator.class);

    private MongoFileManager mm;


    public Annotator() {
        this.mm = MongoFileManager.getInstance(false);
    }

    /**
     * Process annotations following the annotator_type.
     * two options : multithread or single
     */
    public void annotate(Processings annotator_type) {
        try {
            if (AnnotateProperties.isIsMultiThread()) {
                annotateTeiCollectionMultiThreaded(annotator_type);
            } else {
                annotateTeiCollection(annotator_type);
            }
        } catch (UnreachableAnnotateServiceException | AnnotatorNotAvailableException e) {
            LOGGER.error("Error when annotating. ", e);

        }
    }

    /**
     * Annotates TEICorpus collection by annotator_type.
     */
    private void annotateTeiCollection(Processings annotator_type)
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        int nb = 0;
        // Note: why not MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS all the time?
        if (!Processings.contains(annotator_type.getName())) {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) {
                if (mm.initObjects(null, getQuery(AnnotateProperties.isReset(), annotator_type))){
                    while (mm.hasMore()) {
                        BiblioObject biblioObject = mm.nextBiblioObject();
                        if (!AnnotateProperties.isReset() && mm.isProcessed(annotator_type)) {
                            LOGGER.info("\t\t Already annotated by " + annotator_type + ", Skipping...");
                            continue;
                        }
                        Runnable worker = null;
                        if (annotator_type == Processings.NERD) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                worker = new NerdAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Processings.KEYTERM) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                if(biblioObject.getGrobidTei()==null)
                                    biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                    worker = new KeyTermAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Processings.QUANTITIES) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                if(biblioObject.getGrobidTei()==null)
                                    biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                worker = new QuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Processings.PDFQUANTITIES) {
                            if (biblioObject.getIsWithFulltext()) {
                                BinaryFile bf = new BinaryFile();
                                bf.setStream(mm.getFulltext(biblioObject));
                                biblioObject.setPdf(bf);
                                worker = new PDFQuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No fulltext available for " + biblioObject.getRepositoryDocId());
                            }
                        }
                        if (worker != null) {
                            worker.run();
                            nb++;
                        }
                    }
                }
            }
            LOGGER.info("Total: " + nb + " documents annotated.");
        } finally {
            mm.close();
        }
    }

    /**
     * Annotates TEICorpus collection entries (multithread process).
     */
    private void annotateTeiCollectionMultiThreaded(Processings annotator_type)
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        int nb = 0;
        boolean loadPDF = false;
        if (!Processings.contains(annotator_type.getName())) {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) {
                ThreadPoolExecutor executor = getThreadsExecutor(annotator_type);

                if (mm.initObjects(null, getQuery(AnnotateProperties.isReset(), annotator_type))) {
                    //LOGGER.info("processing teis for :" + date);
                    while (mm.hasMore()) {
                        BiblioObject biblioObject = mm.nextBiblioObject();
                        if (!AnnotateProperties.isReset() && mm.isProcessed(annotator_type)) {
                            LOGGER.info("\t\t Already annotated by " + annotator_type + ", Skipping...");
                            continue;
                        }

                        // filter based on document size... we should actually annotate only 
                        // a given length and then stop
                        if (biblioObject.getTeiCorpus().length() > 300000) {
                            LOGGER.info("skipping " + biblioObject.getRepositoryDocId() + ": file too large");
                            continue;
                        }
                        Runnable worker = null;
                        if (annotator_type == Processings.NERD) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                worker = new NerdAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Processings.KEYTERM) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                if(biblioObject.getGrobidTei()==null)
                                    biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                worker = new KeyTermAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Processings.QUANTITIES) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                if(biblioObject.getGrobidTei()==null)
                                    biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                    worker = new QuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Processings.PDFQUANTITIES) {
                            BinaryFile bf = new BinaryFile();
                            bf.setStream(mm.getFulltext(biblioObject));
                            //dont run it if stream is null
                            if (biblioObject.getIsWithFulltext() && bf.getStream()!=null) {
                                biblioObject.setPdf(bf);
                                worker = new PDFQuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                LOGGER.info("\t\t No fulltext available for " + biblioObject.getRepositoryDocId());
                            }
                        }
                        if (worker != null) {
                            executor.execute(worker);
                            nb++;
                        }
                    }
                }
                executor.shutdown();
                LOGGER.info("Jobs done, shutting down thread pool. The executor will wait 2 minutes before forcing the shutdown.");
                try {
                    if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
                LOGGER.info("Finished all threads");
                LOGGER.info("Total: " + nb + " documents annotated.");
            }
        } finally {
            mm.close();
        }
    }

    private DBObject getQuery(boolean reset, Processings annotator_type) {
        DBObject filter = null;
        if (annotator_type == Processings.NERD) {
            if(reset)
                filter = MongoFileManager.ONLY_TRANSFORMED_METADATA;
            else
                filter = MongoFileManager.ONLY_NOT_NERD_ANNOTATED_TRANSFORMED_METADATA;
        } else if (annotator_type == Processings.KEYTERM) {
            if(reset)
                filter = MongoFileManager.ONLY_TRANSFORMED_METADATA;
            else
                filter = MongoFileManager.ONLY_NOT_KEYTERM_ANNOTATED_TRANSFORMED_METADATA;
        } else if (annotator_type == Processings.QUANTITIES) {
            if(reset)
                filter = MongoFileManager.ONLY_TRANSFORMED_METADATA;
            else
                filter = MongoFileManager.ONLY_NOT_QUANTITIES_ANNOTATED_TRANSFORMED_METADATA;
        } else if (annotator_type == Processings.PDFQUANTITIES) {
            if(reset)
                filter = MongoFileManager.ONLY_WITH_FULLTEXT_PROCESS;
            else
                filter = MongoFileManager.ONLY_NOT_PDFQUANTITIES_ANNOTATED_WITH_FULLTEXT;
        }
        return filter;
    }

    /**
     * Returns an execution pool(blocking mode).
     */
    private ThreadPoolExecutor getThreadsExecutor(Processings annotator_type) {
        // max queue of tasks of 50 
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(50);
        int nbThreads = 1;
        if (annotator_type == annotator_type.NERD) {
            nbThreads = AnnotateProperties.getNerdNbThreads();
        } else if (annotator_type == annotator_type.KEYTERM) {
            nbThreads = AnnotateProperties.getKeytermNbThreads();
        } else if (annotator_type == annotator_type.QUANTITIES) {
            nbThreads = AnnotateProperties.getQuantitiesNbThreads();
        } else if (annotator_type == annotator_type.PDFQUANTITIES) {
            nbThreads = AnnotateProperties.getQuantitiesNbThreads();
        }
        LOGGER.info("Number of threads: " + nbThreads);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nbThreads, nbThreads, 60000,
                TimeUnit.MILLISECONDS, blockingQueue);

        // this is for handling rejected tasks (e.g. queue is full)
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                                          ThreadPoolExecutor executor) {
                LOGGER.info("Task Rejected : "
                        + ((AnnotatorWorker) r).getRepositoryDocId());
                LOGGER.info("Waiting for 60 second !!");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    LOGGER.error("Error when interrupting the thread. ", e);
                }
                LOGGER.info("Lets add another time : "
                        + ((AnnotatorWorker) r).getRepositoryDocId());
                executor.execute(r);
            }
        });
        executor.prestartAllCoreThreads();
        return executor;
    }

}
