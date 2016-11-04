package async;

public class LongTask implements Task<Long> {

    @Override
    public Long execute() throws Exception {
        System.out.println("executing mytask");
        java.lang.Long l = new java.lang.Long("-2");
        return l;
    }

}
