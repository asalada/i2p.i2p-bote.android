package i2p.bote.android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Part;

import i2p.bote.Util;
import i2p.bote.android.R;
import i2p.bote.email.Attachment;

public class ContentAttachment implements Attachment {
    private Context mCtx;
    private String mFileName;
    private long mSize;
    private DataHandler mDataHandler;

    public ContentAttachment(Context context, final Uri uri) throws FileNotFoundException {
        mCtx = context;
        // Get the content resolver instance for this context
        ContentResolver cr = context.getContentResolver();

        Cursor returnCursor = cr.query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE},
                null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);

        if (!returnCursor.moveToFirst())
            throw new FileNotFoundException();

        mFileName = returnCursor.getString(nameIndex);
        mSize = returnCursor.getLong(sizeIndex);
        returnCursor.close();

        final String mimeType = cr.getType(uri);
        mDataHandler = new DataHandler(new DataSource() {
            @Override
            public InputStream getInputStream() throws IOException {
                return mCtx.getContentResolver().openInputStream(uri);
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                throw new IOException("Cannot write to attachments");
            }

            @Override
            public String getContentType() {
                return mimeType;
            }

            @Override
            public String getName() {
                return mFileName;
            }
        });
    }

    public ContentAttachment(Context context, Part part)
            throws IOException, MessagingException {
        mCtx = context;
        mFileName = part.getFileName();
        mSize = Util.getPartSize(part);
        mDataHandler = part.getDataHandler();
    }

    @Override
    public String getFileName() {
        return mFileName;
    }

    public long getSize() {
        return mSize;
    }

    public String getHumanReadableSize() {
        int unit = (63-Long.numberOfLeadingZeros(mSize)) / 10;   // 0 if totalBytes<1K, 1 if 1K<=totalBytes<1M, etc.
        double value = (double)mSize / (1<<(10*unit));
        int formatStr;
        switch (unit) {
            case 0: formatStr = R.string.n_bytes; break;
            case 1: formatStr = R.string.n_kilobytes; break;
            default: formatStr = R.string.n_megabytes;
        }
        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
        if (value < 100)
            formatter.setMaximumFractionDigits(1);
        else
            formatter.setMaximumFractionDigits(0);
        return mCtx.getString(formatStr, formatter.format(value));
    }

    @Override
    public DataHandler getDataHandler() {
        return mDataHandler;
    }

    @Override
    public boolean clean() {
        return true;
    }
}
