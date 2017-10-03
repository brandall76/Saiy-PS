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

package ai.saiy.android.utils.debug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Created by benrandall76@gmail.com on 01/09/2016.
 */

public class DebugAction {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = DebugAction.class.getSimpleName();

    public static final int DEBUG_TOGGLE_LOGGING = 0;
    public static final int DEBUG_VALIDATE_SIGNATURE = 1;
    public static final int DEBUG_CLEAR_SYNTHESIS = 2;
    public static final int DEBUG_CLEAR_CUSTOM_COMMANDS = 3;

    /**
     * Remotely validate the signatures
     *
     * @param ctx the application context
     * @return true if the signatures were validated, false otherwise
     */
    public static boolean validateSignatures(@NonNull final Context ctx) {
        final ArrayList<String> signatureArray = DebugAction.getSignatures(ctx);
        // TODO
        return UtilsList.notNaked(signatureArray);
    }

    /**
     * Get the signatures for the application to be validated remotely
     *
     * @param ctx the application context
     * @return an Array List of {@link Signature}
     */
    @SuppressLint("PackageManagerGetSignatures")
    private static ArrayList<String> getSignatures(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSignatures");
        }

        final ArrayList<String> signatureArray = new ArrayList<>();

        try {

            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo packageInfo = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            final Signature[] signatures = packageInfo.signatures;

            CertificateFactory cf;
            X509Certificate cert;
            PublicKey key;
            String mhString;
            int modulusHash;
            if (signatures != null && signatures.length > 0) {

                for (final Signature signature : signatures) {

                    cf = CertificateFactory.getInstance("X.509");
                    cert = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(signature.toByteArray()));
                    key = cert.getPublicKey();
                    modulusHash = ((RSAPublicKey) key).getModulus().hashCode();
                    mhString = String.valueOf(modulusHash)
                            + String.valueOf(ctx.getResources().getInteger(R.integer.hash_version));

                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "hash: " + mhString);
                    }

                    signatureArray.add(mhString);
                }
            }
        } catch (final PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSignature  NameNotFoundException");
                e.printStackTrace();
            }
        } catch (final CertificateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSignature  CertificateException");
                e.printStackTrace();
            }
        } catch (final SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSignature  SecurityException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSignature  NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSignature  Exception");
                e.printStackTrace();
            }
        }

        return signatureArray;
    }
}
