package webService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

public class Upload extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_DIR = "/tmp/";
	public static final String DEFAULT_FILE = "req.txt";
	public static final String DEFAULT_PATH = DEFAULT_DIR + DEFAULT_FILE;

	public boolean isEmptyOrNull(String value) {
	    return value == null || value.trim().length() == 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		boolean foundDescription = false;
		String fileName = "";
		try {
			List<FileItem> items = new ServletFileUpload(
					new DiskFileItemFactory()).parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					
					String fieldname = item.getFieldName();
					String fieldvalue = item.getString();
					if (fieldname.equals("description")) { 
						
						System.out.println("File is: " + fieldvalue);
						foundDescription = true;
						fileName = fieldvalue;
						if (isEmptyOrNull(fieldvalue)){
							fileName = DEFAULT_FILE;
						}
					}
					
				} else {
					
					String fieldname = item.getFieldName();
					String filename = FilenameUtils.getName(item.getName());
					InputStream filecontent = item.getInputStream();
					
					if (foundDescription) {
						FileOutputStream fileToDisk = new FileOutputStream(
								new File(DEFAULT_DIR + fileName));
						int byteRead = filecontent.read();
						while (byteRead != -1) {
							fileToDisk.write(byteRead);
							byteRead = filecontent.read();
						}
						fileToDisk.close();
					}
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("Cannot parse multipart request.", e);
		}
		
		response.sendRedirect("/knn-demo/graph");
	}
}
