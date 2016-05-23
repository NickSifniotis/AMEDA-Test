package au.net.nicksifniotis.amedatest;

/**
 * Created by nsifniotis on 23/05/16.
 */
public class BTConnection {
    private static BTConnection ourInstance = new BTConnection();

    public static BTConnection getInstance() {
        return ourInstance;
    }

    private BTConnection() {
    }
}
