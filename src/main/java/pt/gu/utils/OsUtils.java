package pt.gu.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.system.StructStat;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class OsUtils {

    public enum AuxV {
        AT_NULL(0, R.string.auxv_at_null),
        AT_IGNORE(1, R.string.auxv_at_ignore),
        AT_EXECFD(2, R.string.auxv_at_execfd),
        AT_PHDR(3, R.string.auxv_at_phdr),
        AT_PHENT(4, R.string.auxv_at_phent),
        AT_PHNUM(5, R.string.auxv_at_phnum),
        AT_PAGESZ(6, R.string.auxv_at_pagesz),
        AT_BASE(7, R.string.auxv_at_base),
        AT_FLAGS(8, R.string.auxv_at_flags),
        AT_ENTRY(9, R.string.auxv_at_entry),
        AT_NOTELF(10, R.string.auxv_at_notelf),
        AT_UID(11, R.string.auxv_at_uid),
        AT_EUID(12, R.string.auxv_at_euid),
        AT_GID(13, R.string.auxv_at_gid),
        AT_EGID(14, R.string.auxv_at_egid),
        AT_CLKTCK(17, R.string.auxv_at_clktck),
        AT_PLATFORM(15, R.string.auxv_at_platform),
        AT_HWCAP(16, R.string.auxv_at_hwcap),
        AT_FPUCW(18, R.string.auxv_at_fpucw),
        AT_DCACHEBSIZE(19, R.string.auxv_at_dcachebsize),
        AT_ICACHEBSIZE(20, R.string.auxv_at_icachebsize),
        AT_UCACHEBSIZE(21, R.string.auxv_at_ucachebsize),
        AT_IGNOREPPC(22, R.string.auxv_at_ignoreppc),
        AT_SECURE(23, R.string.auxv_at_secure),
        AT_BASE_PLATFORM(24, R.string.auxv_at_base_platform),
        AT_RANDOM(25, R.string.auxv_at_random),
        AT_HWCAP2(26, R.string.auxv_at_hwcap2),
        AT_EXECFN(31, R.string.auxv_at_execfn),
        AT_SYSINFO(32, R.string.auxv_at_sysinfo),
        AT_SYSINFO_EHDR(33, R.string.auxv_at_sysinfo_ehdr),
        AT_L1I_CACHESHAPE(34, R.string.auxv_at_l1i_cacheshape),
        AT_L1D_CACHESHAPE(35, R.string.auxv_at_l1d_cacheshape),
        AT_L2_CACHESHAPE(36, R.string.auxv_at_l2_cacheshape),
        AT_L3_CACHESHAPE(37, R.string.auxv_at_l3_cacheshape),
        AT_L1I_CACHESIZE(40, R.string.auxv_at_l1i_cachesize),
        AT_L1I_CACHEGEOMETRY(41, R.string.auxv_at_l1i_cachegeometry),
        AT_L1D_CACHESIZE(42, R.string.auxv_at_l1d_cachesize),
        AT_L1D_CACHEGEOMETRY(43, R.string.auxv_at_l1d_cachegeometry),
        AT_L2_CACHESIZE(44, R.string.auxv_at_l2_cachesize),
        AT_L2_CACHEGEOMETRY(45, R.string.auxv_at_l2_cachegeometry),
        AT_L3_CACHESIZE(46, R.string.auxv_at_l3_cachesize),
        AT_L3_CACHEGEOMETRY(47, R.string.auxv_at_l3_cachegeometry);

        private final int id;
        private final int strId;

        AuxV(int i, int j) {
            id = i;
            strId = j;
        }

        public int id(){
            return id;
        }

        public String desc(Context context){
            return context.getString(strId);
        }

        @NonNull
        public static AuxV find(int i){
            for (AuxV v: values()) {
                if (v.id == i)
                    return v;
            }
            return AuxV.AT_NULL;
        }

        public static ArrayMap<AuxV,Long> getauxv(){
            ArrayMap<AuxV,Long> out = new ArrayMap<>();
            ShellUtils.exec(true, x -> {
                String[] s = x.split("\\s+");
                if (s.length > 2){
                    int id = TypeUtils.parseInt(s[1],-1);
                    if (id != -1)
                        out.put(AuxV.find(id),TypeUtils.parseLong(s[2],0));
                }
            },"od -t d8 /proc/%d/auxv",android.os.Process.myPid());
            return out;
        }

    }

    public enum StatMode {

        OTHER(0),GROUP(1),USER(2),ID(3),ACCESS(4);

        private final String[] RWX = {"---","--X","-W-","-WX","R--","R-X","RW-","RWX"};
        private final String[] UGV = {"---","--V","-G-","-GV","U--","U-V","UG-","UGV"};
        private final String[] ACC = {"","Char","Dir","Block","-","File","Link","Socket"};
        private final int mode;

        StatMode(int m){
            this.mode = m;
        }

        public String read(StructStat stat){
            switch (mode){
                case 0: return RWX[stat.st_mode & 7];
                case 1: return RWX[(stat.st_mode >> 3) & 7];
                case 2: return RWX[(stat.st_mode >> 6) & 7];
                case 3: return UGV[(stat.st_mode >> 9) & 7];
                case 4:
                    int i = (stat.st_mode >> 12) & 15;
                    return i % 2 == 0 ? ACC[i/2] : "Fifo " + ACC[i/2];
                default:
                    return null;
            }
        }
    }

    public enum StatDateTime {

        ACCESS(0), MODIFIED(1), CREATE(2);

        private final int mode;

        StatDateTime(int m){
            this.mode = m;
        }

        public String read(StructStat stat){
            switch (mode){
                case 0: return DateTimeUtils.formatDate(stat.st_atime);
                case 1: return DateTimeUtils.formatDate(stat.st_mtime);
                case 2: return DateTimeUtils.formatDate(stat.st_ctime);
                default: return null;
            }
        }
    }

    /**
     *
     * @param context App context
     * @param permissions array of permissions to check
     * @return array of permissions not granted
     */
    @Nullable
    public static String[] checkPermissions(Context context,String... permissions){
        List<String> reqPermissions = new ArrayList<>();
        for (String p : permissions){
            if (context.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED){
                reqPermissions.add(p);
            }
        }
        return reqPermissions.size() == 0 ? null : reqPermissions.toArray(new String[0]);
    }
}