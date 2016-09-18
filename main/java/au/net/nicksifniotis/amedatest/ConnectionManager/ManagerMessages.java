package au.net.nicksifniotis.amedatest.ConnectionManager;

/**
 * Created by Nick Sifniotis on 18/09/16.
 *
 * There are only two sorts of messages that the connection manager will use with the
 * Activity subclasses - SEND and RECEIVE. todo update
 *
 * It is understood that AMEDA instruction packets are what is being sent and received.
 *
 * Some packets are filtered out - namely, HELLO and EHLLO - as these are manager-level
 * instructions that are transparent to the user.
 */
public enum ManagerMessages
{
    SEND, RECEIVE, CONNECTION_DROPPED, CONNECTION_RESTORED;
}
