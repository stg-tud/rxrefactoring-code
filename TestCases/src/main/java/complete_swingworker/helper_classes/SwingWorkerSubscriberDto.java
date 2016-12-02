package complete_swingworker.helper_classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public class SwingWorkerSubscriberDto<ReturnType, ProcessType>
{
    private ReturnType asyncResult;
    private List<ProcessType> chunks;

    SwingWorkerSubscriberDto()
    {
        this.chunks = new ArrayList<ProcessType>();
        asyncResult = null;
    }

    SwingWorkerSubscriberDto<ReturnType, ProcessType> send(ProcessType... chunks )
    {
        synchronized ( this )
        {
            this.chunks.addAll(Arrays.asList(chunks));
        }
        return this;
    }

    SwingWorkerSubscriberDto<ReturnType, ProcessType>  setResult(ReturnType asyncResult )
    {
        this.asyncResult = asyncResult;
        return this;
    }

    List<ProcessType> getChunks()
    {
        synchronized ( this )
        {
            List<ProcessType> chunksCloned = new ArrayList<ProcessType>();
            chunksCloned.addAll(chunks);
            return chunksCloned;
        }
    }

    void removeChunks(List<ProcessType> chunks)
    {
        synchronized ( this )
        {
            this.chunks.removeAll(chunks);
        }
    }

    ReturnType getResult()
    {
        synchronized ( this )
        {
            return this.asyncResult;
        }
    }
}
