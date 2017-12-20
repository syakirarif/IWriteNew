package id.amoled.iwritenew.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id.amoled.iwritenew.R;

/** Fragment ini adalah untuk Trending Fragment.
 * dalam Trending Fragment, akan ditampilkan list artikel yang menjadi trending karena jumlah LIKE dan COMMENT yang banyak
 */

public class TrendingFragment extends Fragment {

    public TrendingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

}
