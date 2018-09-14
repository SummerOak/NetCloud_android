package com.summer.netcloud.traffic;

/**
 * Created by summer on 07/07/2018.
 */

public class IP {

    public static final class DIRECT{
        public static final byte IN = 0;
        public static final byte OUT = 1;
    }

    public static final byte TCP = 6;
    public static final byte UDP = 17;

    public static final class TCPF{
        public static final int TCPF_FIN = 1<<0;
        public static final int TCPF_SYN = 1<<1;
        public static final int TCPF_RST = 1<<2;
        public static final int TCPF_PSH = 1<<3;
        public static final int TCPF_ACK = 1<<4;
        public static final int TCPF_URG = 1<<5;
        public static final int TCPF_ECE = 1<<6;
        public static final int TCPF_CWR = 1<<7;
    }

    public enum TCP_STATE{
        LISTEN,
        SYN_SENT,
        SYN_RCVED,
        ESTABLISHED,
        FIN_WAIT1,
        FIN_WAIT2,
        CLOSE_WAIT,
        CLOSING,
        LAST_ACK,
        TIME_WAIT,
        CLOSED;
    }

    public static final String getProtocolName(byte protocol){
        switch (protocol){
            case IP.TCP: return "TCP";
            case IP.UDP: return "UDP";
        }

        return "?";
    }

    public static final String getStateName(byte protocol, byte state){
        switch (protocol){
            case IP.TCP:{
                switch (IP.TCP_STATE.values()[state]){
                    case LISTEN: return "listen";
                    case SYN_RCVED: return "syn_recv";
                    case SYN_SENT: return "syn_sent";
                    case ESTABLISHED: return "established";
                    case FIN_WAIT1: return "fin_wait1";
                    case FIN_WAIT2: return "fin_wait2";
                    case CLOSING: return "closing";
                    case LAST_ACK: return "last_ack";
                    case CLOSE_WAIT: return "close_wait";
                    case TIME_WAIT: return "time_wait";
                    case CLOSED: return "closed";
                }
                break;
            }

            case IP.UDP:{
                return "";
            }
        }
        return "";
    }

}
