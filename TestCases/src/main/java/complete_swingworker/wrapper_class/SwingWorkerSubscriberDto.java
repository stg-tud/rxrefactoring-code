package complete_swingworker.wrapper_class;

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

    public SwingWorkerSubscriberDto()
    {
        this.chunks = new ArrayList<ProcessType>();
        asyncResult = null;
    }

    public SwingWorkerSubscriberDto<ReturnType, ProcessType> send(ProcessType... chunks )
    {
        synchronized ( this )
        {
            this.chunks.addAll(Arrays.asList(chunks));
        }
        return this;
    }

    public SwingWorkerSubscriberDto<ReturnType, ProcessType>  setResult(ReturnType asyncResult )
    {
//        synchronized ( this )
//        {
//            this.chunks.clear();
//        }
        this.asyncResult = asyncResult;
        return this;
    }

    public List<ProcessType> getChunks()
    {
        synchronized ( this )
        {
            List<ProcessType> chunksCloned = new ArrayList<ProcessType>();
            chunksCloned.addAll(chunks);
            return chunksCloned;
        }
    }

    public void removeChunks(List<ProcessType> chunks)
    {
        synchronized ( this )
        {
            this.chunks.removeAll(chunks);
        }
    }

    public ReturnType getResult()
    {
        synchronized ( this )
        {
            return this.asyncResult;
        }
    }
}
