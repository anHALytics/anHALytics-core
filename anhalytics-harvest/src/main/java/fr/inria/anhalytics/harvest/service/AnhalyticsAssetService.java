package fr.inria.anhalytics.harvest.service;


import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;

/**
 * Grobid assets provider.
 * 
 * @author Achraf
 */
@Path("/")
public class AnhalyticsAssetService {

    private MongoFileManager mm;

    private static String KEY = null ; // :) 
    
    public AnhalyticsAssetService() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
        Properties prop = new Properties();
        try {
            ClassLoader classLoader = AnhalyticsAssetService.class.getClassLoader();
            prop.load(classLoader.getResourceAsStream("harvest.properties"));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file of harvest.properties", exp);
        }
        KEY = prop.getProperty("harvest.service_key");
    }

    @Path("asset")
    @GET
    public Response getImage(@QueryParam("id") String id, @QueryParam("filename") String filename, @QueryParam("key") String key) {
        
        Response response = null;
        if(key.equals(KEY)){
            try {
                InputStream is = mm.findAssetFile(id, filename, MongoCollectionsInterface.GROBID_ASSETS);
                if (is == null) {
                    response = Response.status(Status.NOT_FOUND).build();
                } else {
                    response = Response
                            .ok()
                            .type("image/png")
                            .entity(IOUtils.toByteArray(is))
                            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                            .build();
                }
                is.close();
            } catch (Exception exp) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).type("text/plain").entity(exp.getMessage()).build();
            }
        }else
            response = Response.status(Status.UNAUTHORIZED).type("text/plain").build();
        return response;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "Hello Anhalytics" + "</title>"
                + "<body><h1>" + "Hello Anhalytics" + "</body></h1>" + "</html> ";
    }
}
