package com.unknown.gaelan;

import java.util.Comparator;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Gaelan on 1/2/2015.
 */
public class RankedTrack {

    public static final Comparator<RankedTrack> RANK_COMPARATOR = new Comparator<RankedTrack>() {
        public int compare(RankedTrack lhs, RankedTrack rhs) {
            return rhs.rank - lhs.rank;
        }
    };

    public int rank;
    public Track track;

    public RankedTrack(Track track) {
        this.rank = 1;
        this.track = track;
    }

    public void increment() {
        this.rank++;
    }
}
