package async;

import async.AsyncTaskExecutorTest.Data;

public class GetTask implements Task<Data> {

    @Override
    public Data execute() throws Exception {
        Data data = new Data();
        data.setValue("test");
        return data;
    }

}
