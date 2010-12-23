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

import java.net.*;

/**
 * This class encapsulates a ntp-datagram as described in rfc2030.
 * Such a datagram consists of a header and four timestamps.
 * The four timestamps are respectively: 
 * <UL>
 * <LI>
 * The reference timestamp. This indicates when the local clock was 
 *  last set.
 * Can be set to zero for datagrams originating on the client.
 * <LI>
 * The originate timestamp. Indicates when the datagram originated
 * on the client. Copied by the server from the transmit timestamp.
 * Can be set to zero for datagrams originating on the client.
 * <LI> 
 * The receive timestamp. Indicates when the reply left the server. 
 * Can be set to zero for datagrams originating on the client.
 * <LI>
 * The transmit timestamp. Indicates when the datagram departed.
 * </UL>
 * We have added a fifth timestamp. Namely a 'reception timestamp' 
 * which is normally set by NtpConnection.receive(NtpDatagramPacket).
 * When transmitted a ntp-datagram is wrapped in a UDP datagram.
 * @see java.net.Datagram
 * @see TimeStamp
 * @see NtpHeader
 * @see NtpConnection
 */
public class NtpDatagramPacket  {
    private static final int headerOffset = 0;
    private static final int referenceTimeStampOffset = 16;
    private static final int originateTimeStampOffset = 24;
    private static final int receiveTimeStampOffset = 32;
    private static final int transmitTimeStampOffset = 40;
    private static final int ntpDatagramLength = 48;
    private DatagramPacket dp;
    private TimeStamp receptionTimeStamp;
    /**
   * Construct a NtpDatagram from a header, four timestamps, an 
   * Inetaddress and a portnumber.
   * @see InetAddress
   */
    public NtpDatagramPacket(NtpHeader header, TimeStamp referenceTimeStamp, TimeStamp originateTimeStamp, TimeStamp receiveTimeStamp, TimeStamp transmitTimeStamp, InetAddress iaddr, int iport) {
        byte[] temp;
        byte[] buffer = new byte[ntpDatagramLength];

        for (int i = headerOffset; i < referenceTimeStampOffset; i++) {
            buffer[i] = (header.getData())[i - headerOffset];
        }

        for (int i = referenceTimeStampOffset; i < originateTimeStampOffset; i++) {
            temp = referenceTimeStamp.getData();
            buffer[i] = temp[i - referenceTimeStampOffset];
        }

        for (int i = originateTimeStampOffset; i < receiveTimeStampOffset; i++) {
            temp = originateTimeStamp.getData();
            buffer[i] = temp[i - originateTimeStampOffset];
        }

        for (int i = receiveTimeStampOffset; i < transmitTimeStampOffset; i++) {
            temp = receiveTimeStamp.getData();
            buffer[i] = temp[i - receiveTimeStampOffset];
        }

        for (int i = transmitTimeStampOffset; i < ntpDatagramLength; i++) {
            temp = transmitTimeStamp.getData();
            buffer[i] = temp[i - transmitTimeStampOffset];
        }
        dp = new DatagramPacket(buffer, ntpDatagramLength, iaddr, iport);
    }

    /** 
   * Construct a NtpDatagram with only the transmit timestamp
   * filled in (set to the current time). The header is set to a 
   * NtpHeader.defaultHeader.
   * @see NtpHeader
   */
    public NtpDatagramPacket(InetAddress iaddr, int iport) {
        this(NtpHeader.defaultHeader, TimeStamp.zero, TimeStamp.zero, TimeStamp.zero, new TimeStamp(), iaddr, iport);
    }

    /**
   * Constructs an uninitialized NtpDatagram.
   */
    public NtpDatagramPacket() {
        byte[] buffer = new byte[ntpDatagramLength];
        dp = new DatagramPacket(buffer, ntpDatagramLength);
    }

    /** 
   * Constructs an uninitialized NtpDatagram from a UDP datagram.
   */
    public NtpDatagramPacket(DatagramPacket dp) {
        this.dp = dp;
    }

    /**
   * Returns the UDP datagram associated to an NtpDatagram.
   */
    DatagramPacket getDatagramPacket() {
        return dp;
    }

    /**
   * Returns the header associated to a NtpDatagram.
   * @see NtpHeader
   */
    public NtpHeader getHeader() {
        byte[] buffer = dp.getData();
        byte[] temp = new byte[16];
        for (int i = headerOffset; i < referenceTimeStampOffset; i++) {
            temp[i - headerOffset] = buffer[i];
        }
        return new NtpHeader(temp);
    }

    /**
   * Returns the reference timestamp.
   */
    public TimeStamp getReferenceTimeStamp() {
        byte[] buffer = dp.getData();
        byte[] temp = new byte[8];
        for (int i = referenceTimeStampOffset; i < originateTimeStampOffset; i++) {
            temp[i - referenceTimeStampOffset] = buffer[i];
        }
        return new TimeStamp(temp);
    }

    /**
   * Returns the originate timestamp
   */
    public TimeStamp getOriginateTimeStamp() {
        byte[] buffer = dp.getData();
        byte[] temp = new byte[8];
        for (int i = originateTimeStampOffset; i < receiveTimeStampOffset; i++) {
            temp[i - originateTimeStampOffset] = buffer[i];
        }
        return new TimeStamp(temp);
    }

    /**
   * Returns the receive timestamp
   */
    public TimeStamp getReceiveTimeStamp() {
        byte[] buffer = dp.getData();
        byte[] temp = new byte[8];
        for (int i = receiveTimeStampOffset; i < transmitTimeStampOffset; i++) {
            temp[i - receiveTimeStampOffset] = buffer[i];
        }
        return new TimeStamp(temp);
    }

    /**
   * Returns the transmit timestamp
   */
    public TimeStamp getTransmitTimeStamp() {
        byte[] buffer = dp.getData();
        byte[] temp = new byte[8];
        for (int i = transmitTimeStampOffset; i < ntpDatagramLength; i++) {
            temp[i - transmitTimeStampOffset] = buffer[i];
        }
        return new TimeStamp(temp);
    }

    /**
   * Returns the reception timestamp
   */
    public TimeStamp getReceptionTimeStamp() {
        return receptionTimeStamp;
    }

    void setReceptionTimeStamp(TimeStamp receptionTimeStamp) {
        this.receptionTimeStamp = receptionTimeStamp;
    }

    /**
   * A convenience method which returns the useful information
   * contained in a NtpDatagram.
   * @see NtpInfo
   */
    public NtpInfo getInfo() {
        NtpInfo info = new NtpInfo();
        NtpHeader h = getHeader();
        info.serverAddress = dp.getAddress();
        info.leapYearIndicator = h.getLeapYearIndicator();
        info.versionNumber = h.getVersionNumber();
        info.stratum = h.getStratum();
        info.mode = h.getMode();
        info.pollInterval = h.getPollInterval();
        info.precision = h.getPrecision();
        info.rootDelay = h.getRootDelay();
        info.rootDispersion = h.getRootDispersion();
        info.referenceIdentifier = h.getReferenceIdentifier();
        info.referenceTimeStamp = getReferenceTimeStamp();
        long originate = getOriginateTimeStamp().getTime().getTime();
        long receive = getReceiveTimeStamp().getTime().getTime();
        long transmit = getTransmitTimeStamp().getTime().getTime();
        long reception = getReceptionTimeStamp().getTime().getTime();
        info.roundTripDelay = receive - originate + reception - transmit;
        info.offset = (receive - originate - reception + transmit) / 2;
        return info;
    }

    public String toString() {
        String s;
        s = "Header : ";
        s = s + getHeader();
        s = s + "\n";
        s = s + "ReferenceTimeStamp : ";
        s = s + getReferenceTimeStamp();
        s = s + "\n";
        s = s + "OriginateTimeStamp : ";
        s = s + getOriginateTimeStamp();
        s = s + "\n";
        s = s + "ReceiveTimeStamp : ";
        s = s + getReceiveTimeStamp();
        s = s + "\n";
        s = s + "TransmitTimeStamp : ";
        s = s + getTransmitTimeStamp();
        return s;
    }
}
