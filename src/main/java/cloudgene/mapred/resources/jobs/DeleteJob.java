package cloudgene.mapred.resources.jobs;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.AbstractJob;

public class DeleteJob extends ServerResource {

	@Post
	protected Representation post(Representation entity, Variant variant) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		Form form = new Form(entity);
		String id = form.getFirstValue("id");

		if (id != null) {

			// delete job from database
			JobDao dao = new JobDao();
			AbstractJob job = dao.findById(id);

			if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
				setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
				return new StringRepresentation("Access denied.");
			}

			job.cleanUp();
			dao.delete(job);

			JsonConfig config = new JsonConfig();
			config.setExcludes(new String[] { "user", "outputParams",
					"inputParams", "output", "endTime", "startTime", "error",
					"s3Url", "task", "config", "mapReduceJob", "job", "step",
					"context" });
			JSONObject object = JSONObject.fromObject(job, config);

			return new StringRepresentation(object.toString());

		} else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("Job " + id + " not found.");

		}

	}

}