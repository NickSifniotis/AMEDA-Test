package au.net.nicksifniotis.amedatest.ConnectionManager;

/**
 * Created by Nick Sifniotis on 18/09/16.
 *
 * The messages that the connection manager uses to communicate with the activities.
 *
 * SEND - Activity -> Manager : Please transmit the attahed AMEDA Instruction.
 * RECEIVE - Manager -> Activity : The AMEDA has transmitted the attached AMEDA Response.
 * CONNECTION_DROPPED - Manager -> Activity : We have lost the connection to the device.
 * CONNECTION_RESTORED - Manager -> Activity : Connection to the device has been (re)established.
 *
 * It is understood that AMEDA instruction packets are what is being sent and received.
 *
 */
public enum ManagerMessages
{
    SEND, RECEIVE, CONNECTION_DROPPED, CONNECTION_RESTORED
}
