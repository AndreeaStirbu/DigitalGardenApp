package com.example.digitalgarden.Fragments;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Fragment that is holding common code for handling Image selection
 */
public class PhotoFragment extends Fragment {
    //Constants
    String[] SOIL_TYPES = new String[] {"Moist", "Dry", "Slightly moist"};
    String[] LIGHT_LEVEL = new String[] {"Full sun", "Partial sun", "Dapped sun", "Shade"};

    static final int STORAGE_PERMISSION_CODE = 10;
    static final int CAMERA_PERMISSION_CODE = 11;

    private static final int RESULT_LOAD_IMAGE = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;

    // Image variables
    Uri imgUri;
    ImageView imgView;

    /**
     * Method that handles opening the camera
     */
    void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imgUri = FileProvider.getUriForFile(getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Method that handles opening the external storage
     */
    void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
    }

    /**
     * {@inheritDoc}
     * Once the camera or the gallery is open, process the result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 0 :
                if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
                    imgUri = data.getData();
                    setUriGallery();
                }
                break;
            case 1:
                if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                    this.getActivity().getContentResolver().notifyChange(imgUri, null);
                    setUriCamera();
                }
                break;
        }
    }

    /**
     * Load the imgUri into the ImageView after image has been selected from Gallery
     */
    void setUriGallery() {
        imgView.setImageURI(imgUri);
    }

    /**
     * Load the imgUri into the ImageView after the image has been taken with the Camera
     */
    void setUriCamera() {
        ContentResolver cr = getActivity().getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imgUri);
            imgView.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load", e);
        }
    }

    /**
     * Get the extension of the image selected from the Gallery
     * @param uri The Uri of the image
     * @return The extension of the image
     */
    String getExtension(Uri uri) {
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    /**
     * Creates a File for the image that has been taken with the camera in order to keep the quality
     * and the resolution
     * @return A File for the image
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
    }
}
