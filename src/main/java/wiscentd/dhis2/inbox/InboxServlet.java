package wiscentd.dhis2.inbox;

import org.apache.log4j.Logger;
import wiscentd.dhis2.inbox.model.ConfigurationFile;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/upload")
@MultipartConfig
public class InboxServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(InboxServlet.class.getName());

    private String BASE_PATH, HARDCODED_TOKEN;

    public InboxServlet() {
        ConfigurationFile configurationFile = new ConfigurationFile("inbox.properties");

        // Get BASE_PATH, we expect $WISCENTD_HOME to be initialized
        // If it's not we use CATALINA_HOME to default root
        String WISCENTD_HOME = System.getenv("WISCENTD_HOME");
        if (WISCENTD_HOME == null) WISCENTD_HOME = System.getenv("CATALINA_HOME");

        // The final upload folder name is gathered from ConfigurationFiles
        // ConfigurationFiles can override the logic by adding an absolute path to the configuration file
        Path rootPath = Paths.get(configurationFile.getProperty("base_path"));
        if (rootPath.isAbsolute()) BASE_PATH = rootPath.toString();
        else BASE_PATH = Paths.get(WISCENTD_HOME, BASE_PATH).toString();

        // Ensure working dir exists
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) baseDir.mkdirs();

        // Gather hard-coded token from configuration file
        HARDCODED_TOKEN = configurationFile.getProperty("hardcoded_token");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Implement proper token usage
        if (!request.getHeader("token").equals(HARDCODED_TOKEN)) return;

        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        final Part filePart = request.getPart("file");
        final String fileName = getFileName(filePart);

        try {
            OutputStream out = new FileOutputStream(new File(BASE_PATH + File.separator
                    + fileName));
            InputStream fileContent = filePart.getInputStream();

            int read;
            final byte[] bytes = new byte[1024];

            while ((read = fileContent.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            logger.debug("File + " + fileName + "being uploaded to " + BASE_PATH);

            out.close();
            fileContent.close();

            response.setStatus(200);
        } catch (FileNotFoundException fne) {
            final PrintWriter writer = response.getWriter();
            writer.println("You either did not specify a file to upload or are "
                    + "trying to upload a file to a protected or nonexistent "
                    + "location.");
            writer.println("<br/> ERROR: " + fne.getMessage());
            writer.close();
            logger.error("Problems during file upload. Error: " + fne.getMessage());
        }
    }

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        logger.debug("Part Header = " + partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}