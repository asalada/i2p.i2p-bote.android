package i2p.bote.android.config;

import java.io.IOException;
import java.security.GeneralSecurityException;

import i2p.bote.android.R;
import i2p.bote.android.util.BoteHelper;
import i2p.bote.email.EmailIdentity;
import i2p.bote.fileencryption.PasswordException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewIdentityFragment extends Fragment {
    public static final String IDENTITY_KEY = "identity_key";

    private String mKey;
    private EmailIdentity mIdentity;

    ImageView mIdentityPicture;
    TextView mNameField;
    TextView mDescField;
    TextView mCryptoField;
    TextView mKeyField;

    public static ViewIdentityFragment newInstance(String key) {
        ViewIdentityFragment f = new ViewIdentityFragment();
        Bundle args = new Bundle();
        args.putString(IDENTITY_KEY, key);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_identity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIdentityPicture = (ImageView) view.findViewById(R.id.identity_picture);
        mNameField = (TextView) view.findViewById(R.id.public_name);
        mDescField = (TextView) view.findViewById(R.id.description);
        mCryptoField = (TextView) view.findViewById(R.id.crypto_impl);
        mKeyField = (TextView) view.findViewById(R.id.key);

        mKey = getArguments().getString(IDENTITY_KEY);
        if (mKey != null) {
            try {
                mIdentity = BoteHelper.getIdentity(mKey);
            } catch (PasswordException e) {
                // TODO Handle
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Handle
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                // TODO Handle
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mIdentity != null) {
            Bitmap picture = BoteHelper.decodePicture(mIdentity.getPictureBase64());
            if (picture != null)
                mIdentityPicture.setImageBitmap(picture);

            mNameField.setText(mIdentity.getPublicName());
            mDescField.setText(mIdentity.getDescription());
            mCryptoField.setText(mIdentity.getCryptoImpl().getName());
            mKeyField.setText(mKey);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_identity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_edit_identity:
            Intent ei = new Intent(getActivity(), EditIdentityActivity.class);
            ei.putExtra(EditIdentityFragment.IDENTITY_KEY, mKey);
            startActivity(ei);
            return true;

        case R.id.action_delete_identity:
            DialogFragment df = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.delete_identity)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                BoteHelper.deleteIdentity(mKey);
                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();
                            } catch (PasswordException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (GeneralSecurityException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    return builder.create();
                }
            };
            df.show(getActivity().getSupportFragmentManager(), "deletecontact");
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }
}