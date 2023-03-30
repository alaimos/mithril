/*
 * DSI utilities
 *
 * Copyright (C) 2002-2023 Sebastiano Vigna
 *
 * This program and the accompanying materials are made available under the
 * terms of the GNU Lesser General Public License v2.1 or later,
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html,
 * or the Apache Software License 2.0, which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: LGPL-2.1-or-later OR Apache-2.0
 */

package it.unimi.dsi;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * All-purpose static-method container class.
 *
 * @author Sebastiano Vigna
 * @since 0.1
 */
public final class Util {
    /**
     * A static reference to {@link Runtime#getRuntime()}.
     */
    public static final Runtime RUNTIME = Runtime.getRuntime();
    /**
     * A reasonable format for real numbers. Shared by all format methods.
     */
    private static final NumberFormat FORMAT_DOUBLE = NumberFormat.getInstance(Locale.US);
    /**
     * A reasonable format for integers. Shared by all format methods.
     */
    private static final NumberFormat FORMAT_LONG = NumberFormat.getInstance(Locale.US);
    private static final FieldPosition UNUSED_FIELD_POSITION = new java.text.FieldPosition(0);

    static {
        if (FORMAT_DOUBLE instanceof DecimalFormat) ((DecimalFormat) FORMAT_DOUBLE).applyPattern("#,##0.00");
    }

    static {
        if (FORMAT_DOUBLE instanceof DecimalFormat) ((DecimalFormat) FORMAT_LONG).applyPattern("#,###");
    }

    private Util() {
    }

    /**
     * Formats a number.
     *
     * <P>This method formats a double separating thousands and printing just two fractional digits.
     * <p>Note that the method is synchronized, as it uses a static {@link NumberFormat}.
     *
     * @param d a number.
     * @return a string containing a pretty print of the number.
     */
    public synchronized static @NotNull String format(final double d) {
        return FORMAT_DOUBLE.format(d, new StringBuffer(), UNUSED_FIELD_POSITION).toString();
    }

    /**
     * Formats a number.
     *
     * <P>This method formats a long separating thousands.
     * <p>Note that the method is synchronized, as it uses a static {@link NumberFormat}.
     *
     * @param l a number.
     * @return a string containing a pretty print of the number.
     */
    public synchronized static @NotNull String format(final long l) {
        return FORMAT_LONG.format(l, new StringBuffer(), UNUSED_FIELD_POSITION).toString();
    }

    /**
     * Formats a size.
     *
     * <P>This method formats a long using suitable unit multipliers (e.g., <code>K</code>, <code>M</code>,
     * <code>G</code>, and <code>T</code>)
     * and printing just two fractional digits.
     * <p>Note that the method is synchronized, as it uses a static {@link NumberFormat}.
     *
     * @param l a number, representing a size (e.g., memory).
     * @return a string containing a pretty print of the number using unit multipliers.
     */
    public static @NotNull String formatSize(final long l) {
        if (l >= 1000000000000L) return format(l / 1000000000000.0) + "T";
        if (l >= 1000000000L) return format(l / 1000000000.0) + "G";
        if (l >= 1000000L) return format(l / 1000000.0) + "M";
        if (l >= 1000L) return format(l / 1000.0) + "K";
        return Long.toString(l);
    }

    /**
     * Tries to compact memory as much as possible by forcing garbage collection.
     */
    public static void compactMemory() {
        try {
            @SuppressWarnings("MismatchedReadAndWriteOfArray") final byte[][] unused = new byte[128][];
            for (int i = unused.length; i-- != 0; ) unused[i] = new byte[2000000000];
        } catch (final OutOfMemoryError ignore) {
        }
        System.gc();
    }

}
