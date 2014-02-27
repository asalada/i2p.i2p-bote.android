package i2p.bote;

import java.security.GeneralSecurityException;
import java.util.List;

import i2p.bote.email.Email;
import i2p.bote.fileencryption.PasswordException;
import i2p.bote.folder.EmailFolder;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class FolderFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<Email>> {
    public static final String FOLDER_NAME = "folder_name";

    private static final int EMAIL_LIST_LOADER = 1;

    private EmailListAdapter mAdapter;
    private EmailFolder mFolder;

    public static FolderFragment newInstance(String folderName) {
        FolderFragment f = new FolderFragment();
        Bundle args = new Bundle();
        args.putString(FOLDER_NAME, folderName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new EmailListAdapter(getActivity());
        String folderName = getArguments().getString(FOLDER_NAME);
        mFolder = BoteHelper.getMailFolder(folderName);

        setListAdapter(mAdapter);

        if (mFolder == null) {
            setEmptyText(getResources().getString(
                    R.string.folder_does_not_exist));
            getActivity().setTitle(getResources().getString(R.string.app_name));
        } else {
            setListShown(false);
            setEmptyText(getResources().getString(
                    R.string.folder_empty));
            try {
                getActivity().setTitle(
                        BoteHelper.getFolderDisplayName(getActivity(), mFolder, false));
            } catch (PasswordException e) {
                // TODO: Get password from user and retry
                getActivity().setTitle("ERROR: " + e.getMessage());
            } catch (GeneralSecurityException e) {
                // TODO: Handle properly
                getActivity().setTitle("ERROR: " + e.getMessage());
            }
            getLoaderManager().initLoader(EMAIL_LIST_LOADER, null, this);
        }
    }

    // LoaderManager.LoaderCallbacks<List<Email>>

    public Loader<List<Email>> onCreateLoader(int id, Bundle args) {
        return new EmailListLoader(getActivity(), mFolder);
    }

    public void onLoadFinished(Loader<List<Email>> loader,
            List<Email> data) {
        mAdapter.setData(data);
        try {
            getActivity().setTitle(
                    BoteHelper.getFolderDisplayName(getActivity(), mFolder, true));
        } catch (PasswordException e) {
            // TODO: Get password from user and retry
            getActivity().setTitle("ERROR: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            // TODO: Handle properly
            getActivity().setTitle("ERROR: " + e.getMessage());
        }

        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    public void onLoaderReset(Loader<List<Email>> loader) {
        mAdapter.setData(null);
        try {
            getActivity().setTitle(
                    BoteHelper.getFolderDisplayName(getActivity(), mFolder, false));
        } catch (PasswordException e) {
            // TODO: Get password from user and retry
            getActivity().setTitle("ERROR: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            // TODO: Handle properly
            getActivity().setTitle("ERROR: " + e.getMessage());
        }
    }
}