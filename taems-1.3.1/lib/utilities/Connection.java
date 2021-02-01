package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Connection {
  public static final int RAW = 0;
  
  public static final int KQML = 1;
  
  public static final int LENGTHPREFIX = 2;
  
  protected transient Socket socket;
  
  protected transient BufferedReader input = null;
  
  protected transient BufferedWriter output = null;
  
  protected boolean def = false;
  
  protected String delim = "\r\nEOM\r\n";
  
  protected int type;
  
  protected boolean accepted;
  
  public Connection(Socket s, int t, boolean a) throws IOException {
    this.socket = s;
    this.type = t;
    this.accepted = a;
    switch (t) {
      case 0:
        setDelim("\000");
        break;
      case 1:
        setDelim("\r\nEOM\r\n");
        break;
    } 
    if (this.socket != null) {
      this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    } 
  }
  
  public Connection(Socket s, int t, String d, boolean a) throws IOException {
    this.socket = s;
    this.type = t;
    setDelim(d);
    this.accepted = a;
    if (this.socket != null) {
      this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.output = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    } 
  }
  
  public int getType() {
    return this.type;
  }
  
  public void setType(int t) {
    this.type = t;
  }
  
  public String getRemoteHostName() {
    if (this.socket != null)
      return this.socket.getInetAddress().getHostName(); 
    return null;
  }
  
  public String getLocalHostName() {
    if (this.socket != null)
      return this.socket.getLocalAddress().getHostName(); 
    return null;
  }
  
  public int getRemotePort() {
    if (this.socket != null)
      return this.socket.getPort(); 
    return -1;
  }
  
  public int getLocalPort() {
    if (this.socket != null)
      return this.socket.getLocalPort(); 
    return -1;
  }
  
  public BufferedReader getInput() {
    return this.input;
  }
  
  public BufferedWriter getOutput() {
    return this.output;
  }
  
  public Socket getSocket() {
    return this.socket;
  }
  
  public void setDelim(String d) {
    this.delim = d;
  }
  
  public String getDelim() {
    return this.delim;
  }
  
  public synchronized void close() throws IOException {
    if (this.socket != null) {
      this.output.flush();
      this.output.close();
      this.output = null;
      this.input.close();
      this.input = null;
      this.socket.close();
      this.socket = null;
    } 
  }
  
  public boolean wasAccepted() {
    return this.accepted;
  }
  
  public void setDefault(boolean d) {
    this.def = d;
  }
  
  public boolean isDefault() {
    return this.def;
  }
  
  public String toString() {
    return "(Connection) Rem>" + getRemoteHostName() + ":" + getRemotePort() + " Loc>" + getLocalHostName() + ":" + getLocalPort() + " [Type:" + getType() + " Dflt:" + isDefault() + " Acpt:" + wasAccepted() + "]";
  }
}
