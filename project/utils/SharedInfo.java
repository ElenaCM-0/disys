package utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/* the lock and ReentrantLock are to make sure only one thread
can access or modify shared data at a time. This helps avoid
data being changed by multiple threads at once, which can cause errors
*/

import p2p.P2PConnection;


public class SharedInfo {
    private final Lock lock; // is used to make sure that only one thread at a time can access or modify the answer or waitingConnection
    private Boolean answer;
    private P2PConnection waitingConnection; // an object that stores some kind of connection or request


    // this constructor sets up the lock and puts answer and waitingConnection to null (empty)
    public SharedInfo() {
        this.lock = new ReentrantLock();
        this.answer = null;
        this.waitingConnection = null;
    }



    // -------- LOCK -------- //

    // esure that when one thread accesses this object, no other thread can change it until the first one is done
    public void acquireLock() {
        lock.lock();
    }


    // allows other threads to access the object after the current thread is finished.
    public void releaseLock() {
        lock.unlock();
    }



    // -------- ANSWER -------- //

    // returns the value stored in answer
    public Boolean getAnswer() {
        return answer;
    }

    // lets you update the answer to something new
    public void setAnswer(Boolean answer) {
        this.answer = answer;
    }



    // -------- WAITING_CONNECTION -------- //

    // returns the current connection or request stored
    public P2PConnection getWaitingConnection() {
        return waitingConnection;
    }

    // updates waitingConnection with a new connection/request
    public void setWaitingConnection(P2PConnection waitingConnection) {
        this.waitingConnection = waitingConnection;
    }
}
