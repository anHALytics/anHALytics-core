package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.exceptions.UnreachableAnnotateServiceException;
import fr.inria.anhalytics.annotate.exceptions.AnnotatorNotAvailableException;
import fr.inria.anhalytics.annotate.services.AnnotateService;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.UnknownHostException;
import java.util.concurrent.*;

/**
 * Handles threads used to annotate the tei collection from MongoDB.
 *
 * @author Patrice
 */
public class Annotator {

    private static final Logger logger = LoggerFactory.getLogger(Annotator.class);

    private final MongoFileManager mm;

    public enum Annotator_Type {

        NERD("NERD"),
        KEYTERM("KeyTerm");

        private String name;

        private Annotator_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public Annotator() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }

    public void annotate(Annotator_Type annotator_type) {
        try {
            if (AnnotateProperties.isIsMultiThread()) {
                annotateTeiCollectionMultiThreaded(annotator_type);
            } else {
                annotateTeiCollection(annotator_type);
            }
        } catch (Exception e) {
            logger.error("Error when setting-up the annotator.");
            e.printStackTrace();
        }
    }

    /**
     * Annotates tei collection entries with fulltext.
     */
    private void annotateTeiCollection(Annotator_Type annotator_type)
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        int nb = 0;
        String annotationsCollection = null, teiCollection = null;
        if (annotator_type == Annotator_Type.NERD) {
            annotationsCollection = MongoCollectionsInterface.NERD_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.FINAL_TEIS;
        } else if (annotator_type == Annotator_Type.KEYTERM) {
            annotationsCollection = MongoCollectionsInterface.KEYTERM_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.GROBID_TEIS;
        } else {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) 
            {
                for (String date : Utilities.getDates()) {

                    if (!AnnotateProperties.isProcessByDate()) {
                        date = null;
                    }
                    if (mm.initTeis(date, true, teiCollection)) {
                        logger.debug("processing teis for :" + date);
                        while (mm.hasMoreTeis()) {
                            String tei = mm.nextTeiDocument();
                            String id = mm.getCurrentRepositoryDocId();
                            String anhalyticsId = mm.getCurrentAnhalyticsId();
                            boolean isWithFulltext = mm.isCurrentIsWithFulltext();
                            if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                                logger.info("skipping " + id + " No anHALytics id provided");
                                continue;
                            }
                            // check if the document is already annotated
                            if (!AnnotateProperties.isReset()) {
                                if (mm.isAnnotated(annotationsCollection)) {
                                    logger.debug("skipping " + id + ": already annotated");
                                    continue;
                                }
                            }

                            // filter based on document size... we should actually annotate only 
                            // a given length and then stop
                            if (tei.length() > 300000) {
                                logger.debug("skipping " + id + ": file too large");
                                continue;
                            }
                            Runnable worker = null;
                            if (annotator_type == Annotator_Type.NERD) {
                                worker = new NerdAnnotatorWorker(mm, id, anhalyticsId, tei, date);
                            } else if (annotator_type == Annotator_Type.KEYTERM) {
                                worker = new KeyTermAnnotatorWorker(mm, id, anhalyticsId, tei, date);
                            }
                            worker.run();
                            nb++;
                        }
                    }
                    if (!AnnotateProperties.isProcessByDate()) {
                        break;
                    }
                }
            }
            logger.debug("Total: " + nb + " documents annotated.");
        } finally {
            mm.close();
        }
    }

    /**
     * Annotates tei collection entries with fulltext (multithread process).
     */
    private void annotateTeiCollectionMultiThreaded(Annotator_Type annotator_type)
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        int nb = 0;
        String annotationsCollection = null, teiCollection = null;
        if (annotator_type == Annotator_Type.NERD) {
            annotationsCollection = MongoCollectionsInterface.NERD_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.FINAL_TEIS;
        } else if (annotator_type == Annotator_Type.KEYTERM) {
            annotationsCollection = MongoCollectionsInterface.KEYTERM_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.GROBID_TEIS;
        } else {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) {
                ThreadPoolExecutor executor = getThreadsExecutor(annotator_type);
                for (String date : Utilities.getDates()) {
                    if (!AnnotateProperties.isProcessByDate()) {
                        date = null;
                    }
                    if (mm.initTeis(date, true, teiCollection)) {
                        //logger.debug("processing teis for :" + date);
                        while (mm.hasMoreTeis()) {
                            String tei = mm.nextTeiDocument();
                            String repositoryDocId = mm.getCurrentRepositoryDocId();
                            String anhalyticsId = mm.getCurrentAnhalyticsId();
                            
                            if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                                logger.info("skipping " + repositoryDocId + " No anHALytics id provided");
                                continue;
                            }
                            // check if the document is already annotated
                            if (!AnnotateProperties.isReset()) {
                                if (mm.isAnnotated(annotationsCollection)) {
                                    logger.debug("skipping " + repositoryDocId + ": already annotated");
                                    continue;
                                }
                            }

                            // filter based on document size... we should actually annotate only 
                            // a given length and then stop
                            if (tei.length() > 300000) {
                                logger.debug("skipping " + repositoryDocId + ": file too large");
                                continue;
                            }
                            Runnable worker = null;
                            if (annotator_type == Annotator_Type.NERD) {
                                worker = new NerdAnnotatorWorker(mm, repositoryDocId, anhalyticsId, tei, date);
                            } else {
                                worker = new KeyTermAnnotatorWorker(mm, repositoryDocId, anhalyticsId, tei, date);
                            }

                            executor.execute(worker);
                            nb++;
                        }
                    }
                    if (!AnnotateProperties.isProcessByDate()) {
                        break;
                    }
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                }
                logger.info("Finished all threads");
                logger.debug("Total: " + nb + " documents annotated.");
            }
        } finally {
            mm.close();
        }
    }

    private ThreadPoolExecutor getThreadsExecutor(Annotator_Type annotator_type) {
        // max queue of tasks of 50 
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(50);
        int nbThreads = 1;
        if (annotator_type == annotator_type.NERD) {
            nbThreads = AnnotateProperties.getNerdNbThreads();
        } else if (annotator_type == annotator_type.KEYTERM) {
            nbThreads = AnnotateProperties.getKeytermNbThreads();
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nbThreads, nbThreads, 60000,
                TimeUnit.MILLISECONDS, blockingQueue);

        // this is for handling rejected tasks (e.g. queue is full)
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                logger.info("Task Rejected : "
                        + ((AnnotatorWorker) r).getRepositoryDocId());
                logger.info("Waiting for 60 second !!");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Lets add another time : "
                        + ((AnnotatorWorker) r).getRepositoryDocId());
                executor.execute(r);
            }
        });
        executor.prestartAllCoreThreads();
        return executor;
    }

}
