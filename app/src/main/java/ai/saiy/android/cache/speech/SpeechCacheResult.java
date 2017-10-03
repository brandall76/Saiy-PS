/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.cache.speech;

/**
 * Helper class to store the results of the speech cache
 * <p>
 * Created by benrandall76@gmail.com on 27/04/2016.
 */
public class SpeechCacheResult {

    private final byte[] compressedBytes;
    private final long rowId;
    private final boolean success;

    public SpeechCacheResult(final byte[] compressedBytes, final long rowId, final boolean success) {
        this.compressedBytes = compressedBytes;
        this.rowId = rowId;
        this.success = success;
    }

    public byte[] getCompressedBytes() {
        return compressedBytes;
    }

    public long getRowId() {
        return rowId;
    }

    public boolean isSuccess() {
        return success;
    }
}
