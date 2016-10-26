package oracle9_ForkJoin;

import java.util.concurrent.RecursiveAction;

/**
 * Created: 19.10.16 creation date
 */
public class ForkBlur extends RecursiveAction
{
    private int[] mSource;
    private int mStart;
    private int mLength;
    private int[] mDestination;

    // Processing window size; should be odd.
    private int mBlurWidth = 15;
    protected static int sThreshold = 100000;

    public ForkBlur(int[] src, int start, int length, int[] dst)
    {
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst;
    }

    protected void computeDirectly()
    {
        int sidePixels = (mBlurWidth - 1) / 2;
        for ( int index = mStart; index < mStart + mLength; index++ )
        {
            // Calculate average.
            float rt = 0, gt = 0, bt = 0;
            for ( int mi = -sidePixels; mi <= sidePixels; mi++ )
            {
                int mindex = Math.min(Math.max(mi + index, 0),
                        mSource.length - 1);
                int pixel = mSource[ mindex ];
                rt += (float) ((pixel & 0x00ff0000) >> 16)
                        / mBlurWidth;
                gt += (float) ((pixel & 0x0000ff00) >> 8)
                        / mBlurWidth;
                bt += (float) ((pixel & 0x000000ff) >> 0)
                        / mBlurWidth;
            }

            // Reassemble destination pixel.
            int dpixel = (0xff000000) |
                    (((int) rt) << 16) |
                    (((int) gt) << 8) |
                    (((int) bt) << 0);
            mDestination[ index ] = dpixel;
        }
    }

    @Override
    protected void compute()
    {
        if ( mLength < sThreshold )
        {
            computeDirectly();
            return;
        }

        int split = mLength / 2;

        invokeAll(
                new ForkBlur(mSource, mStart, split, mDestination),
                new ForkBlur(mSource, mStart + split, mLength - split, mDestination));
    }
}


// Create a task that represents all of the work to be done.
// source image pixels are in src
// destination image pixels are in dst
//      ForkBlur fb = new ForkBlur(src, 0, src.length, dst);

// Create the ForkJoinPool that will run the task.
//      ForkJoinPool pool = new ForkJoinPool();

// Run the task.
//      pool.invoke(fb);