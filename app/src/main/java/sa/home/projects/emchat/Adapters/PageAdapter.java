package sa.home.projects.emchat.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import sa.home.projects.emchat.Fragments.ChatsFragment;
import sa.home.projects.emchat.Fragments.FriendsFragment;
import sa.home.projects.emchat.Fragments.RequestsFragment;

public class PageAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>(
            Arrays.asList(new RequestsFragment(), new ChatsFragment(), new FriendsFragment()));

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
