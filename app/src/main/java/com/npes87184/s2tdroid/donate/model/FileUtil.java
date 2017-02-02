package com.npes87184.s2tdroid.donate.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for helping parsing file systems.
 * From jeisfeld/Augendiagnose
 */
public final class FileUtil {
    /**
     * The name of the primary volume (LOLLIPOP).
     */
    private static final String PRIMARY_VOLUME_NAME = "primary";

    private Context context;

    /**
     * Hide default constructor.
     */
    public FileUtil(Context context) {
        this.context = context;
    }


    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static boolean isWritable(@NonNull final File file) {
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            }
            catch (IOException e) {
                // do nothing.
            }
        }
        catch (FileNotFoundException e) {
            // in android, if file not found, it will create it auto.
        }

        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return result;
    }

    /**
     * Check for a directory if it is possible to create files within this directory via normal writing
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    public static  boolean isWritableNormal(@Nullable final File folder) {
        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        }
        while (file.exists());

        return isWritable(file);
    }

    // Utility methods for Android 5

    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    public boolean isWritableNormalOrSaf(@Nullable final File folder, Uri uri) {
        // Find a non-existing file in this directory.
        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        }
        while (file.exists());

        // First check regular writability
        if (isWritable(file)) {
            return true;
        }

        // Next check SAF writability.
        DocumentFile document = null;
        try {
            document = getDocumentFile(file, false, uri);
        }
        catch (Exception e) {
            return false;
        }

        if (document == null) {
            return false;
        }

        // This should have created the file - otherwise something is wrong with access URL.
        boolean result = document.canWrite() && file.exists();

        // Ensure that the dummy file is not remaining.
        document.delete();

        return result;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isOnExtSdCard(@NonNull final File file) {
        return getExtSdCardFolder(file) != null;
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file              The file.
     * @param isDirectory       flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    public DocumentFile getDocumentFile(@NonNull final File file, final boolean isDirectory, Uri uri) {
        Uri[] treeUris = new Uri[1];
        treeUris[0] = uri;
        Uri treeUri = null;

        if (treeUris.length == 0) {
            return null;
        }

        String fullPath;
        try {
            fullPath = file.getCanonicalPath();
        }
        catch (IOException e) {
            return null;
        }

        String baseFolder = null;

        // First try to get the base folder via unofficial StorageVolume API from the URIs.

        for (int i = 0; baseFolder == null && i < treeUris.length; i++) {
            String treeBase = getFullPathFromTreeUri(treeUris[i]);
            if (treeBase != null && fullPath.startsWith(treeBase)) {
                treeUri = treeUris[i];
                baseFolder = treeBase;
            }
        }

        if (baseFolder == null) {
            // Alternatively, take root folder from device and assume that base URI works.
            treeUri = treeUris[0];
            baseFolder = getExtSdCardFolder(file);
        }

        if (baseFolder == null) {
            return null;
        }

        String relativePath = fullPath.substring(baseFolder.length() + 1);

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if (i < parts.length - 1 || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    /**
     * Get the full path of a document from its tree URI.
     *
     * @param treeUri The tree RI.
     * @return The path (without trailing file separator).
     */
    @Nullable
    private String getFullPathFromTreeUri(@Nullable final Uri treeUri) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = getVolumePath(FileUtil.getVolumeIdFromTreeUri(treeUri));
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = FileUtil.getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            }
            else {
                return volumePath + File.separator + documentPath;
            }
        }
        else {
            return volumePath;
        }
    }

    /**
     * Get the volume ID from the tree URI.
     *
     * @param treeUri The tree URI.
     * @return The volume ID.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        }
        else {
            return null;
        }
    }

    /**
     * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
     *
     * @param treeUri The tree URI.
     * @return the document path.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        }
        else {
            return File.separator;
        }
    }

    /**
     * Get the path of a certain volume.
     *
     * @param volumeId The volume id.
     * @return The path.
     */
    private String getVolumePath(final String volumeId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getExtSdCardFolder(@NonNull final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    // Unexpected external file dir
                }
                else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

}
