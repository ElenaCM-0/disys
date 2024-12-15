package utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/* the lock and ReentrantLock are to make sure only one thread
can access or modify shared data at a time. This helps avoid
data being changed by multiple threads at once, which can cause errors
*/

import p2p.P2PConnection;

/**
 * Class that represents info that its shared between the thread main and other
 * threads corresponding to P2P Connections.
 */
public class SharedInfo {
    private final Lock lock; // is used to make sure that only one thread at a time can access or modify the
                             // answer or waitingConnection
    private Boolean answer; // answer to a certain question a thread asks to main. It's null while main hasn
                            // answered
    private P2PConnection waitingConnection; // Indicates which connection has made the question

    /**
     * this constructor sets up the lock and puts answer and waitingConnection to
     * null (empty)
     */
    public SharedInfo() {
        this.lock = new ReentrantLock();
        this.answer = null;
        this.waitingConnection = null;
    }

    // -------- LOCK -------- //

    /**
     * Ensure that when one thread accesses this object, no other thread can change
     * it until the first one is done
     */
    public void acquireLock() {
        lock.lock();
    }

    /**
     * Allows other threads to access the object after the current thread is
     * finished.
     */
    public void releaseLock() {
        lock.unlock();
    }

    // -------- ANSWER -------- //

    /**
     * @return the value stored in answer
     */
    public Boolean getAnswer() {
        return answer;
    }

    /**
     * Changes the value of answer
     * 
     * @param answer new value
     */
    public void setAnswer(Boolean answer) {
        this.answer = answer;
    }

    // -------- WAITING_CONNECTION -------- //

    /**
     * @return the current connection or request stored
     */
    public P2PConnection getWaitingConnection() {
        return waitingConnection;
    }

    /**
     * Updates waitingConnection with a new connection/request
     * 
     * @param waitingConnection new connection
     */
    public void setWaitingConnection(P2PConnection waitingConnection) {
        this.waitingConnection = waitingConnection;
    }
}
