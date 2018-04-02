package wiscentd.dhis2.inbox;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@WebServlet("/upload")
@MultipartConfig()
public class InboxServlet extends HttpServlet {
    private String BASE_PATH, HARDCODED_TOKEN;

    public InboxServlet() {
        try {
            Properties properties = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("inbox.properties");
            properties.load(is);
            BASE_PATH = properties.getProperty("base_path");
            BASE_PATH = Paths.get(!BASE_PATH.startsWith("/") ? System.getenv("DHIS2_HOME") : "", BASE_PATH).toString();

            HARDCODED_TOKEN = properties.getProperty("hardcoded_token");
            File baseDir = new File(BASE_PATH);
            if (!baseDir.exists()) baseDir.mkdirs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // TODO: Implement proper token usage
        if (!request.getHeader("token").equals(HARDCODED_TOKEN)) return;

        String creationModality = request.getHeader("creationModality");
        String instance = request.getHeader("instance");
        String title = instance + "-" + creationModality + "-" + new SimpleDateFormat("yyMMdd-HHmmss")
                .format(Calendar.getInstance().getTime());

        storeFile(title + "-dataValueSets.json", request.getPart("jsonResult").getInputStream());

        response.sendRedirect(request.getContextPath());
    }

    private void storeFile(String fileName, InputStream fileContent) throws IOException {
        File targetFile = new File(BASE_PATH + fileName);
        OutputStream outputStream = new FileOutputStream(targetFile);

        int read;
        byte[] bytes = new byte[1024];

        while ((read = fileContent.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        outputStream.close();
    }
}