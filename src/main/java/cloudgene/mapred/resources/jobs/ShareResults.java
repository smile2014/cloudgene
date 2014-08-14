package cloudgene.mapred.resources.jobs;

import genepi.io.FileUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.Settings;

public class ShareResults extends ServerResource {

	private static final Log log = LogFactory.getLog(ShareResults.class);

	@Get
	public Representation get() {

		    String username = (String) getRequest().getAttributes().get("username");
		    String hash = (String) getRequest().getAttributes().get("hash");
			String filename = (String) getRequest().getAttributes().get(
					"filename");

			DownloadDao dao = new DownloadDao();
			Download download = dao.findByHash(hash);

			if (download == null){
				setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return new StringRepresentation(
						"download file file not..");

			}
			
			MediaType mediaType = MediaType.ALL;
			if (filename.endsWith(".zip")) {
				mediaType = MediaType.APPLICATION_ZIP;
			} else if (filename.endsWith(".txt") || filename.endsWith(".csv")) {
				mediaType = MediaType.TEXT_PLAIN;
			} else if (filename.endsWith(".pdf")) {
				mediaType = MediaType.APPLICATION_PDF;
			} else if (filename.endsWith(".html")) {
				mediaType = MediaType.TEXT_HTML;
			}

			Settings settings = Settings.getInstance();
			String workspace = settings.getLocalWorkspace(username);
	
			String resultFile = FileUtil.path(workspace, "output",
					download.getPath());

			if (download.getCount() > 0) {

				log.debug("Downloading file " + resultFile);

				download.decCount();
				dao.update(download);

				return new FileRepresentation(resultFile, mediaType);

			} else {

				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return new StringRepresentation(
						"number of max downloads exceeded.");

			}


	}

}