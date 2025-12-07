package sorgente.LogInSignUp.LoadingData;

public class GlobalProgressManager
{
    private static ProgressListener listener;
    public static boolean isInitialLoading = true;

    public static void setListener(ProgressListener l)
    {
        listener = l;
    }

    public static void notifyProgress(int progress) {
        if (listener != null) {
            listener.onProgress(progress);
        }
    }
}
