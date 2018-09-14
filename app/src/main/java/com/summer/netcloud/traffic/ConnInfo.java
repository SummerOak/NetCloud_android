package com.summer.netcloud.traffic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 14/06/2018.
 */

public class ConnInfo {
    public int id;
    public int uid;
    public byte protocol;
    public byte state;

    public long accept = 0l;
    public long back = 0l;
    public long sent = 0l;
    public long recv = 0l;

    public String dest;
    public int destPort;

    public long born_time;
    public boolean alive;

    public List<TCPLog> tcp_logs = new ArrayList<>();

}
