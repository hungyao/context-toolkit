/***************************************************************************/
/* COACH: Component Based Open Source Architecture for                     */
/*        Distributed Telecom Applications                                 */
/* See:   http://www.objectweb.org/                                        */
/*                                                                         */
/* Copyright (C) 2003 Lucent Technologies Nederland BV                     */
/*                    Bell Labs Advanced Technologies - EMEA               */
/*                                                                         */
/* Initial developer(s): Harold Batteram                                   */
/*                                                                         */
/* This library is free software; you can redistribute it and/or           */
/* modify it under the terms of the GNU Lesser General Public              */
/* License as published by the Free Software Foundation; either            */
/* version 2.1 of the License, or (at your option) any later version.      */
/*                                                                         */
/* This library is distributed in the hope that it will be useful,         */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of          */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU        */
/* Lesser General Public License for more details.                         */
/*                                                                         */
/* You should have received a copy of the GNU Lesser General Public        */
/* License along with this library; if not, write to the Free Software     */
/* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */
/***************************************************************************/
package org.coach.tracing.service.ntp;

import java.io.*;
import java.net.*;
import java.util.*;

public class NtpConnection  {
    // Default port for the NTP protocol. 
    public static final int defaultNtpPort = 123;
    private InetAddress ntpServer;
    private int ntpPort;
    private DatagramSocket datagramSocket;
    private int maxHops = 15;
    private int timeout = 10000;
    public NtpConnection(InetAddress iaddr, int iport) throws SocketException {
        ntpServer = iaddr;
        ntpPort = iport;
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(timeout);
    }

    public NtpConnection(InetAddress iaddr) throws SocketException {
        ntpServer = iaddr;
        ntpPort = defaultNtpPort;
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(timeout);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) throws SocketException {
        this.timeout = timeout;
        datagramSocket.setSoTimeout(timeout);
    }

    public void send(NtpDatagramPacket ntpDatagramPacket) throws IOException {
        datagramSocket.send(ntpDatagramPacket.getDatagramPacket());
    }

    public void receive(NtpDatagramPacket ntpDatagramPacket) throws IOException {
        datagramSocket.receive(ntpDatagramPacket.getDatagramPacket());
        ntpDatagramPacket.setReceptionTimeStamp(new TimeStamp(new Date()));
    }

    public NtpInfo getInfo() throws IOException {
        NtpDatagramPacket dpSend = new NtpDatagramPacket(ntpServer, ntpPort);
        NtpDatagramPacket dpReceive = new NtpDatagramPacket();
        send(dpSend);
        receive(dpReceive);
        return dpReceive.getInfo();
    }

    /**
   *  Traces a server to the primary server.
   *  @return Vector containing the NtpInfo objects associated with 
   *  the servers on the path to the primary server. Sometimes only a 
   *   partial list will be generated due to timeouts or other problems.
   */
    public Vector<NtpInfo> getTrace() {
        Vector<NtpInfo> traceList = new Vector<NtpInfo>();
        int hops = 0;
        boolean finished = false;
        NtpConnection currentNtpConnection = this;
        while ((!finished) && (hops < maxHops)) {
            try {
                NtpInfo info = currentNtpConnection.getInfo();

                if (currentNtpConnection != this) {
                    currentNtpConnection.close();
                }
                traceList.addElement(info);

                if (info.referenceIdentifier instanceof InetAddress) {
                    currentNtpConnection = new NtpConnection((InetAddress)info.referenceIdentifier);
                    hops++;
                } else {
                    finished = true;
                }
            } catch (Exception e) {
                finished = true;
            }
        }
        return traceList;
    }

    /**
   * Get the time from the server.
   * @return A Date object containing the server time, adjusted for 
   * roundtrip delay. Note that it is better to use getInfo() and then 
   * to use the offset field of the returned NtpInfo object.
   */
    public Date getTime() {
        try {
            long offset = getInfo().offset;
            return new Date(System.currentTimeMillis() + offset);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        datagramSocket.close();
    }

    public void finalize() {
        close();
    }
}

