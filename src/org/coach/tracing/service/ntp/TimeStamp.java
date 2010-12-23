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

import java.util.*;

/**
 *  This class encapsulates the notion of a timestamp as in rfc2030.
 *  Logically it is the number of seconds since the beginning of the
 *  centure (in UTC time).
 *  It is represented as  an 8 byte array, the first four bytes 
 *  representing seconds and the next 4 bytes representing second
 *  fractions.
*/
public class TimeStamp  {
    private long integerPart;
    private long fractionalPart;
    private byte[] data;
    private Date date;
    static private TimeZone UTC = new SimpleTimeZone(0, "UTC");
    static private Calendar c = new GregorianCalendar(1900, Calendar.JANUARY, 1, 0, 0, 0);
    static private Date startOfCentury;
    static
    {
        c.setTimeZone(UTC);
        startOfCentury = c.getTime();
    }

    private static final byte[] emptyArray = { 0, 0, 0, 0, 0, 0, 0, 0 };
    /**
   * The timestamp corresponding to the beginning of the century.
   */
    public static final TimeStamp zero = new TimeStamp(emptyArray);
    private int mp(byte b) {
        int bb = b;
        return (bb < 0) ? 256 + bb : bb;
    }

    public TimeStamp() {
        this(new Date());
    }

    public TimeStamp(Date date) {
        data = new byte[8];
        this.date = date;
        long msSinceStartOfCentury = date.getTime() - startOfCentury.getTime();
        integerPart = msSinceStartOfCentury / 1000;
        fractionalPart = ((msSinceStartOfCentury % 1000) * 0x100000000L) / 1000;
        long temp = integerPart;

        for (int i = 3; i >= 0; i--) {
            data[i] = (byte)(temp % 256);
            temp = temp / 256;
        }
        temp = fractionalPart;

        for (int i = 7; i >= 4; i--) {
            data[i] = (byte)(temp % 256);
            temp = temp / 256;
        }
    }

    public TimeStamp(byte[] data) {
        this.data = data;
        integerPart = 0;
        @SuppressWarnings("unused")
		int u;

        for (int i = 0; i <= 3; i++) {
            integerPart = 256 * integerPart + mp(data[i]);
        }
        fractionalPart = 0;

        for (int i = 4; i <= 7; i++) {
            fractionalPart = 256 * fractionalPart + mp(data[i]);
        }
        long msSinceStartOfCentury = integerPart * 1000 + (fractionalPart * 1000) / 0x100000000L;
        date = new Date(msSinceStartOfCentury + startOfCentury.getTime());
    }

    public boolean equals(TimeStamp ts) {
        boolean value = true;
        byte[] tsData = ts.getData();
        for (int i = 0; i <= 7; i++) {

            if (data[i] != tsData[i]) {
                value = false;
            }
        }
        return value;
    }

    public String toString() {
        return "" + date + " + " + fractionalPart + "/" + 0x100000000L;
    }

    public byte[] getData() {
        return data;
    }

    public Date getTime() {
        return date;
    }
}
