package nl.knaw.dans.easy.web.view.dataset;

import java.util.List;

import nl.knaw.dans.common.lang.repo.DmoStoreId;
import nl.knaw.dans.common.lang.service.exceptions.CommonSecurityException;
import nl.knaw.dans.common.lang.service.exceptions.ObjectNotAvailableException;
import nl.knaw.dans.common.lang.service.exceptions.ServiceException;
import nl.knaw.dans.common.wicket.exceptions.InternalWebError;
import nl.knaw.dans.easy.domain.dataset.FileItemDescription;
import nl.knaw.dans.easy.domain.dataset.item.ItemVO;
import nl.knaw.dans.easy.domain.deposit.discipline.KeyValuePair;
import nl.knaw.dans.easy.domain.model.Dataset;
import nl.knaw.dans.easy.servicelayer.services.Services;
import nl.knaw.dans.easy.web.EasyResources;
import nl.knaw.dans.easy.web.EasySession;
import nl.knaw.dans.easy.web.common.DatasetModel;
import nl.knaw.dans.easy.web.template.AbstractDatasetModelPanel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoPanel extends AbstractDatasetModelPanel implements IHeaderContributor
{

    private static final long serialVersionUID = -5695419613165470561L;
    private static final Logger logger = LoggerFactory.getLogger(VideoPanel.class);

    private String streamingUrls = "";
    private String ticketValue = "";

    private boolean initiated;

    public VideoPanel(String id, DatasetModel model, final List<ItemVO> videoFiles, PageParameters pageParameters)
    {
        super(id, model);

        final Dataset dataset = getDataset();

            add(new Label("fileName", ""));
            add(new Label("streamingUrl", ""));
        	for (ItemVO videoFile : videoFiles) {
        		addVideoFile(videoFile, dataset);
        	}
    }

    public boolean isInitiated()
    {
        return initiated;
    }

    @Override
    protected void onBeforeRender()
    {
        if (!initiated)
        {
            init();
            initiated = true;
        }
        super.onBeforeRender();
    }

    @Override
    public void renderHead(IHeaderResponse response)
    {
        response.renderString("<script> var presentation = \"" + streamingUrls + "\"; </script><script> var ticket_value = \"" + ticketValue + "\"; </script>");
    }
    
    private void init()
    {
    }


    
    private void addVideoFile(final ItemVO videoFile, final Dataset dataset)
    {
        try
        {
		    replace(new Label("fileName", videoFile.getName()));
		    FileItemDescription description = Services.getItemService().getFileItemDescription(EasySession.getSessionUser(), dataset,
		            new DmoStoreId(videoFile.getSid()));
		    List<KeyValuePair> metadata = description.getMetadataForAnonKnown();
		    for (KeyValuePair kvp : metadata)
		    {
		        if (kvp.getKey().toLowerCase().equals("streamingurl"))
		        {
		            replace(new Label("streamingUrl", kvp.getValue()));
		            streamingUrls = kvp.getValue();
		            ticketValue = "lippu1";
//		            streamingUrls += kvp.getValue() + ";";
		            break;
		        }
		    }
        }
        catch (ObjectNotAvailableException e)
        {
            errorMessage(EasyResources.NOT_FOUND);
            logger.error("Object not found: ", e);
            throw new InternalWebError();
        }
        catch (CommonSecurityException e)
        {
            final String message = errorMessage(EasyResources.INTERNAL_ERROR);
            logger.error(message, e);
            throw new InternalWebError();
        }
        catch (ServiceException e)
        {
            final String message = errorMessage(EasyResources.INTERNAL_ERROR);
            logger.error(message, e);
            throw new InternalWebError();
        }
    }
}
