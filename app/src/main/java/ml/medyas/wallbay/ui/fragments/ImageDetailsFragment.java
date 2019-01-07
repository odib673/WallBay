package ml.medyas.wallbay.ui.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import ml.medyas.wallbay.R;
import ml.medyas.wallbay.databinding.FragmentImageDetailsBinding;
import ml.medyas.wallbay.entities.ImageEntity;
import ml.medyas.wallbay.services.WallpaperService;
import ml.medyas.wallbay.utils.GlideApp;
import ml.medyas.wallbay.utils.Utils;

import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static ml.medyas.wallbay.utils.Utils.drawableToBitmap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnImageDetailsFragmentInteractions} interface
 * to handle interaction events.
 * Use the {@link ImageDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageDetailsFragment extends Fragment {
    public static final String IMAGE_ENTITY = "IMAGE_ENTITY";
    private OnImageDetailsFragmentInteractions mListener;
    private FragmentImageDetailsBinding binding;
    private ImageEntity imageEntity;
    private boolean toggle = true;

    public ImageDetailsFragment() {
        // Required empty public constructor
    }

    //TODO create info sheet with color palette-image details-tags... provided by

    public static ImageDetailsFragment newInstance(ImageEntity imageEntity) {
        ImageDetailsFragment frag = new ImageDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(IMAGE_ENTITY, imageEntity);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageEntity = getArguments().getParcelable(IMAGE_ENTITY);
        }

        postponeEnterTransition();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_image_details, container, false);

        binding.setImage(imageEntity);
        binding.imageDetailInfo.setImage(imageEntity);
        binding.imageDetailInfo.setFragment(this);

        GlideApp.with(this)
                .load(R.drawable.image_loading)
                .into(binding.loadingImage);

        GlideApp.with(this)
                .asDrawable()
                .load(imageEntity.getPreviewImage())
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(binding.photoView);

        binding.imageDetailInfo.itemToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onImageBackPressed();
            }
        });

        binding.photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.imageDetailInfo.getRoot().getVisibility() == View.GONE) {
                    binding.imageDetailInfo.getRoot().setVisibility(View.VISIBLE);
                } else {
                    binding.imageDetailInfo.getRoot().setVisibility(View.GONE);
                }
            }
        });

        return binding.getRoot();
    }

    public void onFabClicked(View view) {
        switch (view.getId()) {
            case R.id.fab_download:
                fabDownload();
                break;

            case R.id.fab_fav:
                favFavorite();
                break;

            case R.id.fab_info:
                break;

            case R.id.fab_edit:
                break;

            case R.id.fab_wall:
                setWallpaper();

                break;

            case R.id.load_original:
                GlideApp.with(this)
                        .load(imageEntity.getOriginalImage())
                        .apply(new RequestOptions()
                                .format(DecodeFormat.PREFER_ARGB_8888))
                        .placeholder(new ColorDrawable(Color.TRANSPARENT))
                        .error(R.drawable.ic_image_black_24dp)
                        .into(binding.photoView);
                return;

            case R.id.image_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TITLE, "Photo by: " + imageEntity.getUserName())
                        .putExtra(Intent.EXTRA_TEXT, "Provided by: " + getProvider(imageEntity.getProvider()) + " - " + imageEntity.getUrl())
                        .setType("image/jpeg")
                        .putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(getContext().getContentResolver(), drawableToBitmap(binding.photoView.getDrawable()), imageEntity.getUserName(), null)));
                startActivity(Intent.createChooser(intent, "Share with"));
                return;

            case R.id.item_plus:
                break;

        }
        toggleFabs();
    }

    private String getProvider(Utils.webSite provider) {
        switch (provider) {
            case PIXABAY:
                return "Pixabay";
            case UNSPLASH:
                return "Unsplash";
            case PEXELS:
                return "Pexels";
        }
        return "";
    }

    private void setWallpaper() {
        Toast.makeText(getContext(), "Loading image ...", Toast.LENGTH_SHORT).show();
        WallpaperService.setWallpaper(getContext(), imageEntity.getOriginalUrl());
    }

    private void favFavorite() {

        mListener.onAddToFavorite(imageEntity).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                Toast.makeText(getContext(), "Image added to Favorite", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getContext(), "could not added image to Favorite", Toast.LENGTH_SHORT).show();
            }
        });

        binding.imageDetailInfo.lottieFav.setProgress(.2f);
        binding.imageDetailInfo.lottieFav.setVisibility(View.VISIBLE);
        binding.imageDetailInfo.lottieFav.playAnimation();
        binding.imageDetailInfo.lottieFav.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                binding.imageDetailInfo.lottieFav.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void fabDownload() {

        if (checkPermission()) {
            downloadImage();
        }
    }

    private void downloadImage() {
        Toast.makeText(getContext(), "Downloading...", Toast.LENGTH_SHORT).show();

        binding.imageDetailInfo.lottieDownload.setVisibility(View.VISIBLE);
        binding.imageDetailInfo.lottieDownload.playAnimation();
        binding.imageDetailInfo.lottieDownload.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                binding.imageDetailInfo.lottieDownload.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        WallpaperService.downloadWallpaper(getContext(), imageEntity.getOriginalUrl(), imageEntity.getProvider().getCode());
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage();
            } else {
                Toast.makeText(getContext(), "Could not get permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleFabs() {
        Display mdisp = getActivity().getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        int maxY = mdispSize.y;

        if (toggle) {

            binding.imageDetailInfo.itemPlus.setImageResource(R.drawable.ic_close_black_24dp);
            AnimatorSet decSet2 = new AnimatorSet();
            decSet2.playTogether(
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabWall, View.ALPHA, 0, 1),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabEdit, View.ALPHA, 0, 1),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabInfo, View.ALPHA, 0, 1),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabDownload, View.ALPHA, 0, 1),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabFav, View.ALPHA, 0, 1),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabWall, "y", maxY - 310),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabEdit, "y", maxY - 310),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabInfo, "y", maxY - 410),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabDownload, "y", maxY - 310),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabFav, "y", maxY - 310)
            );
            decSet2.setDuration(500);
            decSet2.start();
        } else {
            binding.imageDetailInfo.itemPlus.setImageResource(R.drawable.ic_add_circle_outline_black_24dp);
            AnimatorSet decSet2 = new AnimatorSet();
            decSet2.playTogether(
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabWall, View.ALPHA, 1, 0),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabEdit, View.ALPHA, 1, 0),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabInfo, View.ALPHA, 1, 0),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabDownload, View.ALPHA, 1, 0),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabFav, View.ALPHA, 1, 0),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabWall, "y", maxY),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabEdit, "y", maxY),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabInfo, "y", maxY),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabDownload, "y", maxY),
                    ObjectAnimator.ofFloat(binding.imageDetailInfo.fabFav, "y", maxY)
            );
            decSet2.setDuration(500);
            decSet2.start();
        }

        toggle = !toggle;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnImageDetailsFragmentInteractions) {
            mListener = (OnImageDetailsFragmentInteractions) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnImageDetailsFragmentInteractions {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        Completable onAddToFavorite(ImageEntity imageEntity);
        void onImageBackPressed();
    }
}
