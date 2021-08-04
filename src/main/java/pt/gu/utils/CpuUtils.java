package pt.gu.utils;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class CpuUtils {

    private static final String TAG = CpuUtils.class.getSimpleName();
    private static final boolean DBG = false;


    public static void getHwCaps(){
        //return getauxval(16);
    }

    private static native String getAA64mrs();

    private static final ArrayMap<String,Long> AA64MRS = new ArrayMap<>();
    static {
        String s = getAA64mrs();
        String[] s2;
        for (String s1 : s.split("\n")) {
            s2 = s1.split(":");
            AA64MRS.put(s2[0], Long.parseLong(s2[1]));
        }
    }

    private static long getAA64MRS(String id, long defVal){
        Long v = AA64MRS.getOrDefault(id,null);
        return v == null ? defVal : v;
    }

    /** AArch64 Instruction Set Attribute Register 0
     *  Provides information about the instructions implemented in AArch64 state.
     */
    public static class AA64ISAR0_EL1 {

        private static AA64ISAR0_EL1 sClass;

        public static AA64ISAR0_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64ISAR0_EL1();
            return sClass;
        }

        public final long VALUE;

        /** Indicates support for Random Number instructions in AArch64 state.
         *      0b0000  No Random Number instructions are implemented.
         *      0b0001  RNDR and RNDRRS registers are implemented.
         */
        public final int RNDR;

        /** Indicates support for Outer shareable and TLB range maintenance instructions.
         *      0b0000  Outer shareable and TLB range maintenance instructions are not implemented.
         *      0b0001  Outer shareable TLB maintenance instructions are implemented.
         *      0b0010  Outer shareable and TLB range maintenance instructions are implemented.
         *  FEAT_TLBIOS implements the functionality identified by the values 0b0001 and 0b0010.
         *  FEAT_TLBIRANGE implements the functionality identified by the value 0b0010.
         */
        public final int TLB;

        /** Indicates support for flag manipulation instructions. Defined values are:
         *      0b0000  No flag manipulation instructions are implemented.
         *      0b0001  CFINV, RMIF, SETF16, and SETF8 instructions are implemented.
         *      0b0010  CFINV, RMIF, SETF16, SETF8, AXFLAG, and XAFLAG instructions are implemented.
         *  FEAT_FlagM implements the functionality identified by the value 0b0001.
         *  FEAT_FlagM2 implements the functionality identified by the value 0b0010.
         */
        public final int TS;

        /** Indicates support for FMLAL and FMLSL instructions. Defined values are:
         *      0b0000  FMLAL and FMLSL instructions are not implemented.
         *      0b0001  FMLAL and FMLSL instructions are implemented.
         *  FEAT_FHM implements the functionality identified by the value 0b0001.
         */
        public final int FHM;

        /** Indicates support for Dot Product instructions in AArch64 state. Defined values are:
         *      0b0000  No Dot Product instructions implemented.
         *      0b0001  UDOT and SDOT instructions implemented.
         *  FEAT_DotProd implements the functionality identified by the value 0b0001.
         */
        public final int DP;

        /** Indicates support for SM4 instructions in AArch64 state. Defined values are:
         *      0b0000  No SM4 instructions implemented.
         *      0b0001  SM4E and SM4EKEY instructions implemented.
         *  If FEAT_SM4 is not implemented, the value 0b0001 is reserved.
         *  This field must have the same value as ID_AA64ISAR0_EL1.SM3.
         */
        public final int SM4;

        /** Indicates support for SM3 instructions in AArch64 state. Defined values are:
         *      0b0000  No SM3 instructions implemented.
         *      0b0001  SM3SS1, SM3TT1A, SM3TT1B, SM3TT2A, SM3TT2B, SM3PARTW1, and SM3PARTW2 instructions implemented.
         *      If FEAT_SM3 is not implemented, the value 0b0001 is reserved.
         *  FEAT_SM3 implements the functionality identified by the value 0b0001.
         *  This field must have the same value as ID_AA64ISAR0_EL1.SM4.
         */
        public final int SM3;

        /** Indicates support for SHA3 instructions in AArch64 state. Defined values are:
         *      0b0000  No SHA3 instructions implemented.
         *      0b0001  EOR3, RAX1, XAR, and BCAX instructions implemented.
         *      If FEAT_SHA3 is not implemented, the value 0b0001 is reserved.
         *  FEAT_SHA3 implements the functionality identified by the value 0b0001.
         *  If the value of ID_AA64ISAR0_EL1.SHA1 is 0b0000, this field must have the value 0b0000.
         *  If the value of this field is 0b0001, ID_AA64ISAR0_EL1.SHA2 must have the value 0b0010.
         */
        public final int SHA3;

        /** Indicates support for SQRDMLAH and SQRDMLSH instructions in AArch64 state. Defined values are:
         *      0b0000  No RDMA instructions implemented.
         *      0b0001  SQRDMLAH and SQRDMLSH instructions implemented.
         *  FEAT_RDM implements the functionality identified by the value 0b0001.
         */
        public final int RDM;

        /** Indicates support for Atomic instructions in AArch64 state. Defined values are:
         *      0b0000  No Atomic instructions implemented.
         *      0b0010  LDADD, LDCLR, LDEOR, LDSET, LDSMAX, LDSMIN, LDUMAX, LDUMIN, CAS, CASP, and SWP instructions implemented.
         *  FEAT_LSE implements the functionality identified by the value 0b0010.
         */
        public final int Atomic;

        /** Indicates support for CRC32 instructions in AArch64 state. Defined values are:
         *      0b0000  No CRC32 instructions implemented.
         *      0b0001  CRC32B, CRC32H, CRC32W, CRC32X, CRC32CB, CRC32CH, CRC32CW, and CRC32CX instructions implemented.
         */
        public final int CRC32;

        /** Indicates support for SHA2 instructions in AArch64 state. Defined values are:
         *      0b0000   No SHA2 instructions implemented.
         *      0b0001   Implements instructions: SHA256H, SHA256H2, SHA256SU0, and SHA256SU1.
         *      0b0010   Implements instructions:
         *                  SHA256H, SHA256H2, SHA256SU0, and SHA256SU1.
         *                  SHA512H, SHA512H2, SHA512SU0, and SHA512SU1.
         *  FEAT_SHA256 implements the functionality identified by the value 0b0001.
         *  FEAT_SHA512 implements the functionality identified by the value 0b0010.
         *  If the value of ID_AA64ISAR0_EL1.SHA1 is 0b0000, this field must have the value 0b0000.
         *  If the value of this field is 0b0010, ID_AA64ISAR0_EL1.SHA3 must have the value 0b0001.
         */
        public final int SHA2;

        /** Indicates support for SHA1 instructions in AArch64 state. Defined values are:
         *      0b0000  No SHA1 instructions implemented.
         *      0b0001  SHA1C, SHA1P, SHA1M, SHA1H, SHA1SU0, and SHA1SU1 instructions implemented.
         *  FEAT_SHA1 implements the functionality identified by the value 0b0001.
         *  If the value of ID_AA64ISAR0_EL1.SHA2 is 0b0000, this field must have the value 0b0000.
         */
        public final int SHA1;

        /** Indicates support for AES instructions in AArch64 state. Defined values are:
         *      0b0000  No AES instructions implemented.
         *      0b0001  AESE, AESD, AESMC, and AESIMC instructions implemented.
         *      0b0010  As for 0b0001, plus PMULL/PMULL2 instructions operating on 64-bit data quantities.
         *  FEAT_AES implements the functionality identified by the value 0b0001.
         *  FEAT_PMULL implements the functionality identified by the value 0b0010.
         */
        public final int AES;


        AA64ISAR0_EL1(){
            VALUE  = getAA64MRS("ID_AA64ISAR0_EL1",0);
            RNDR   = (int)((VALUE >> 60) & 0xF);
            TLB    = (int)((VALUE >> 56) & 0xF);
            TS     = (int)((VALUE >> 52) & 0xF);
            FHM    = (int)((VALUE >> 48) & 0xF);
            DP     = (int)((VALUE >> 44) & 0xF);
            SM4    = (int)((VALUE >> 40) & 0xF);
            SM3    = (int)((VALUE >> 36) & 0xF);
            SHA3   = (int)((VALUE >> 32) & 0xF);
            RDM    = (int)((VALUE >> 28) & 0xF);
            //[24:27] Reserved, RES0.---------
            Atomic = (int)((VALUE >> 20) & 0xF);
            CRC32  = (int)((VALUE >> 16) & 0xF);
            SHA2   = (int)((VALUE >> 12) & 0xF);
            SHA1   = (int)((VALUE >>  8) & 0xF);
            AES    = (int)((VALUE >>  4) & 0xF);
            //[00:04] Reserved, RES0.---------
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put("AES", AES);
                o.put("SHA1",SHA1);
                o.put("SHA1",SHA2);
                o.put("CRC32",CRC32);
                o.put("Atomic",Atomic);
                o.put("RDM",RDM);
                o.put("SHA3",SHA3);
                o.put("SM3",SM3);
                o.put("SM4",SM4);
                o.put("DP",DP);
                o.put("FHM",FHM);
                o.put("TS",TS);
                o.put("TLB",TLB);
                o.put("RNDR",RNDR);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** AArch64 Instruction Set Attribute Register 1, EL1
     *  provides information about the instructions implemented in AArch64 state.
     */
    public static class AA64ISAR1_EL1 {

        private static AA64ISAR1_EL1 sClass;

        public static AA64ISAR1_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64ISAR1_EL1();
            return sClass;
        }

        public final long VALUE;

        /** Indicates whether load-acquire (LDA) instructions are implemented for a Release Consistent core consistent RCPC model.
         *      0x1     The LDAPRB, LDAPRH, and LDAPR instructions are implemented in AArch64.
         */
        public final int LRCPC;

        /** Indicates whether Data Cache, Clean to the Point of Persistence (DC CVAP) instructions are implemented.
         *      0x1     DC CVAP is supported in AArch64.
         */
        public final int DC_CVAP;

        AA64ISAR1_EL1(){
            VALUE  = getAA64MRS("ID_AA64ISAR1_EL1",0);
            //[63:24] Reserved, RES0.---------
            LRCPC   = (int)((VALUE >> 20) & 0xF);
            //[19:04] Reserved, RES0.---------
            DC_CVAP = (int)(VALUE & 0xF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put("DC_CVAP", DC_CVAP);
                o.put("LRCPC",LRCPC);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** AArch64 Memory Model Feature Register 0, EL1
     *  Provides information about the implemented memory model and memory management support in the AArch64 Execution state.
     */
    public static class AA64MMFR0_EL1 {

        private static AA64MMFR0_EL1 sClass;

        public static AA64MMFR0_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64MMFR0_EL1();
            return sClass;
        }

        public final long VALUE;

        /** Support for 4KB memory translation granule size:
         *      0x0     4KB granule supported.
         */
        public final int TGran4;

        /** Support for 64KB memory translation granule size:
         *      0x0     64KB granule supported.
         */
        public final int TGran64;

        /** Support for 16KB memory translation granule size:
         *      0x1     Indicates that the 16KB granule is supported.
         */
        public final int TGran16;

        /** Mixed-endian support only at EL0.
         *      0x0     No mixed-endian support at EL0.
         *              The SCTLR_EL1.E0E bit has a fixed value.
         */
        public final int BigEndEL0;

        /** Secure versus Non-secure Memory distinction:
         *      0x1     Supports a distinction between Secure and Non-secure Memory.
         */
        public final int SNSMem;

        /** Mixed-endian configuration support:
         *      0x1     Mixed-endian support.
         *              The SCTLR_ELx.EE and SCTLR_EL1.E0E bits can be configured.
         */
        public final int BigEnd;

        /** Number of ASID bits:
         *      0x2     16 bits.
         */
        public final int ASIDBits;

        /** Physical address range supported:
         *      0x4     44 bits, 16TB.
         *              The supported Physical Address Range is 44 bits.
         *              Other cores in the DSU might support a different Physical Address Range.
         */
        public final int PARange;

        AA64MMFR0_EL1(){

            VALUE   = getAA64MRS("ID_AA64MMFR0_EL1",0);
            //[63:32] Reserved, RES0.---------
            TGran4    = (int)((VALUE >> 28) & 0xF);
            TGran64   = (int)((VALUE >> 24) & 0xF);
            TGran16   = (int)((VALUE >> 20) & 0xF);
            BigEndEL0 = (int)((VALUE >> 16) & 0xF);
            SNSMem    = (int)((VALUE >> 12) & 0xF);
            BigEnd    = (int)((VALUE >>  8) & 0xF);
            ASIDBits  = (int)((VALUE >>  4) & 0xF);
            PARange   = (int)(VALUE & 0xF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put("PARange", PARange);
                o.put("ASIDBits",ASIDBits);
                o.put("BigEnd", BigEnd);
                o.put("SNSMem",SNSMem);
                o.put("BigEndEL0", BigEndEL0);
                o.put("TGran16",TGran16);
                o.put("TGran64", TGran64);
                o.put("TGran4",TGran4);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** AArch64 Memory Model Feature Register 1, EL1
     *  Provides information about the implemented memory model and memory management support in the AArch64 Execution state.
     */
    public static class AA64MMFR1_EL1 {

        private static AA64MMFR1_EL1 sClass;

        public static AA64MMFR1_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64MMFR1_EL1();
            return sClass;
        }

        public final long VALUE;

        /** Indicates whether provision of EL0 vs EL1 execute never control at Stage 2 is supported.
         *      0x1	EL0/EL1 execute control distinction at Stage 2 bit is supported.
         */
        public final int XNX;

        /** Privileged Access Never. Indicates support for the PAN bit in PSTATE, SPSR_EL1, SPSR_EL2, SPSR_EL3, and DSPSR_EL0.
         *      0x2     PAN supported and AT S1E1RP and AT S1E1WP instructions supported.
         */
        public final int PAN;

        /** Indicates support for LORegions.
         *      0x1     LORegions are supported.
         */
        public final int LO;

        /** Presence of Hierarchical Disables. Enables an operating system or hypervisor
         * to hand over up to 4 bits of the last level page table descriptor (bits[62:59] of the page table entry)
         * for use by hardware for IMPLEMENTATION DEFINED usage. The value is:
         *      0x2     Hierarchical Permission Disables and Hardware allocation of bits[62:59] supported.
         */
        public final int HD;

        /** Virtualization Host Extensions support
         *      0x1     Virtualization Host Extensions supported.
         */
        public final int VH;

        /** support for number of VMID bits
         *      0x2     16 bits are supported.
         */
        public final int VMID;

        /** support for hardware updates to Access flag and Dirty state in translation tables.
         *      0x2     Hardware update of both the Access flag and dirty state is supported in hardware.
         */
        public final int HAFDBS;

        AA64MMFR1_EL1(){
            VALUE   = getAA64MRS("ID_AA64MMFR1_EL1",0);
            //[63:32] Reserved, RES0.---------
            XNX    = (int)((VALUE >> 28) & 0xF);
            //[28:24] Reserved, RES0.---------
            PAN    = (int)((VALUE >> 20) & 0xF);
            LO     = (int)((VALUE >> 16) & 0xF);
            HD     = (int)((VALUE >> 12) & 0xF);
            VH     = (int)((VALUE >>  8) & 0xF);
            VMID   = (int)((VALUE >>  4) & 0xF);
            HAFDBS = (int)(VALUE & 0xF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put("HAFDBS", HAFDBS);
                o.put("VMID",VMID);
                o.put("VH", VH);
                o.put("HD",HD);
                o.put("LO", LO);
                o.put("PAN",PAN);
                o.put("XNX", XNX);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** AArch64 Processor Feature Register 0, EL1
     *  Provides additional information about implemented core features in AArch64.
     *  https://developer.arm.com/documentation/100403/0200/register-descriptions/aarch64-system-registers/id-aa64pfr0-el1--aarch64-processor-feature-register-0--el1
     *
     */
    public static class AA64PFR0_EL1 {

        private static AA64PFR0_EL1 sClass;

        public static AA64PFR0_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64PFR0_EL1();
            return sClass;
        }

        public final long VALUE;

        /** RAS extension version.
         *      0x1     Version 1 of the RAS extension is present.
         */
        public final int RAS;

        /** GIC CPU interface:
         *      0x0     GIC CPU interface is disabled, GICCDISABLE is HIGH, or not implemented.
         *      0x1     GIC CPU interface is implemented and enabled, GICCDISABLE is LOW.
         */
        public final int GIC;

        /** Advanced SIMD.
         *      0x1     Advanced SIMD, including Half-precision support, is implemented.
         */
        public final int AdvSIMD;

        /** Floating-point.
         *      0x1     Floating-point, including Half-precision support, is implemented.
         */
        public final int FP;

        /** EL3 exception handling:
         *      0x2     Instructions can be executed at EL3 in AArch64 or AArch32 state.
         */
        public final int EL3_handling;

        /** EL2 exception handling:
         *      0x2     Instructions can be executed at EL3 in AArch64 or AArch32 state.
         */
        public final int EL2_handling;

        /** EL1 exception handling:
         *      0x2 	Instructions can be executed at EL3 in AArch64 or AArch32 state.
         */
        public final int EL1_handling;

        /** EL0 exception handling:
         *      0x2	    Instructions can be executed at EL0 in AArch64 or AArch32 state.
         */
        public final int EL0_handling;

        AA64PFR0_EL1(){
            VALUE   = getAA64MRS("ID_AA64PFR0_EL1",0);
            //[63:32] Reserved, RES0.---------
            RAS          = (int)((VALUE >> 28) & 0xF);
            GIC          = (int)((VALUE >> 24) & 0xF);
            AdvSIMD      = (int)((VALUE >> 20) & 0xF);
            FP           = (int)((VALUE >> 16) & 0xF);
            EL3_handling = (int)((VALUE >> 12) & 0xF);
            EL2_handling = (int)((VALUE >>  8) & 0xF);
            EL1_handling = (int)((VALUE >>  4) & 0xF);
            EL0_handling = (int)(VALUE & 0xF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put("EL0_handling", EL0_handling);
                o.put("EL1_handling",EL1_handling);
                o.put("EL2_handling", EL2_handling);
                o.put("EL3_handling",EL3_handling);
                o.put("FP", FP);
                o.put("AdvSIMD",AdvSIMD);
                o.put("GIC", GIC);
                o.put("RAS", RAS);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** AArch64 Processor Feature Register 1, EL1
     *  Provides additional information about implemented core features in AArch64.
     *  https://developer.arm.com/documentation/101392/0000/register-descriptions/aarch64-system-registers/id-aa64pfr1-el1--aarch64-processor-feature-register-1--el1
     */
    public static class AA64PFR1_EL1 {

        private static AA64PFR1_EL1 sClass;

        public static AA64PFR1_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64PFR1_EL1();
            return sClass;
        }

        public final long VALUE;

        /** PSTATE.SSBS. The possible values are:
         *      0x1     AArch64 provides the PSTATE.SSBS mechanism to mark regions that are
         *              Speculative Store Bypassing Safe (SSBS), but does not implement the MSR/MRS instructions
         *              to directly read and write the PSTATE.SSBS field.
         */
        public final int SSBS;

        AA64PFR1_EL1(){
            VALUE   = getAA64MRS("ID_AA64PFR1_EL1",0);
            //[63:32] Reserved, RES0.---------
            SSBS         = (int)((VALUE >> 4) & 0xF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put("SSBS", SSBS);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** AArch64 Debug Feature Register 0, EL1
     *  Provides top-level information about the debug system in AArch64.
     *  https://developer.arm.com/documentation/100403/0200/register-descriptions/aarch64-system-registers/id-aa64dfr0-el1--aarch64-debug-feature-register-0--el1
     */
    public static class AA64DFR0_EL1 {

        private static AA64DFR0_EL1 sClass;

        public static AA64DFR0_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64DFR0_EL1();
            return sClass;
        }

        public final long VALUE;

        /** Number of breakpoints that are context-aware, minus 1. These are the highest numbered breakpoints:
         *      0x1     Two breakpoints are context-aware.
         */
        public final int CTX_CMPs;

        /** The number of watchpoints minus 1:
         *      0x3     Four watchpoints.
         */
        public final int WRPs;

        /** The number of breakpoints minus 1:
         *      0x5     Six breakpoints.
         */
        public final int BRPs;

        /** Performance Monitors Extension version.
         *      0x4 	Performance monitor system registers implemented, PMUv3.
         */
        public final int PMUVer;

        /** Trace extension:
         *      0x0	    Trace system registers not implemented.
         */
        public final int TraceVer;

        /** Debug architecture version:
         *      0x8     ARMv8-A debug architecture implemented.
         */
        public final int DebugVer;

        AA64DFR0_EL1(){
            VALUE   = getAA64MRS("ID_AA64DFR0_EL1",0);
            //[63:32] Reserved, RES0.---------
            CTX_CMPs    = (int)((VALUE >> 28) & 0xF);
            //[27:24] Reserved, RES0.---------
            WRPs        = (int)((VALUE >> 20) & 0xF);
            //[19:16] Reserved, RES0.---------
            BRPs        = (int)((VALUE >> 12) & 0xF);
            PMUVer      = (int)((VALUE >>  8) & 0xF);
            TraceVer    = (int)((VALUE >>  4) & 0xF);
            DebugVer    = (int)(VALUE & 0xF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put(".value",VALUE);
                o.put("DebugVer", DebugVer);
                o.put("TraceVer",TraceVer);
                o.put("PMUVer", PMUVer);
                o.put("BRPs",BRPs);
                o.put("WRPs", WRPs);
                o.put("CTX_CMPs",CTX_CMPs);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }

    }

    /** AArch64 Debug Feature Register 1
     *  Reserved for future expansion of top level information about the debug system in AArch64 state.
     *  https://developer.arm.com/documentation/ddi0595/2020-12/AArch64-Registers/ID-AA64DFR1-EL1--AArch64-Debug-Feature-Register-1
     */
    public static class AA64DFR1_EL1 {

        private static AA64DFR1_EL1 sClass;

        public static AA64DFR1_EL1 getInstance(){
            if (sClass == null)
                sClass = new AA64DFR1_EL1();
            return sClass;
        }

        public final long VALUE;

        AA64DFR1_EL1(){
            VALUE   = getAA64MRS("ID_AA64DFR1_EL1",0);
            //[63:0] Reserved, RES0.---------
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put(".value",VALUE);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }

    }

    /** Multiprocessor Affinity Register, EL1
     *  Provides an additional core identification mechanism for scheduling purposes in a cluster.
     *  https://developer.arm.com/documentation/100403/0301/register-descriptions/aarch64-system-registers/mpidr-el1--multiprocessor-affinity-register--el1
     */
    public static class MPIDR_EL1 {
        private static MPIDR_EL1 sClass;

        public static MPIDR_EL1 getInstance(){
            if (sClass == null)
                sClass = new MPIDR_EL1();
            return sClass;
        }

        public final long VALUE;

        /** Affinity level 3. Highest level affinity field.
         *  CLUSTERID   Indicates the value read in the CLUSTERIDAFF3 configuration signal.
         */
        public final long Aff3;

        /** Indicates a single core system, as distinct from core 0 in a cluster. This value is:
         *  0	Core is part of a multiprocessor system.
         *      This is the value for implementations with more than one core,
         *      and for implementations with an ACE or CHI master interface.
         */
        public final long U;

        /** Indicates whether the lowest level of affinity consists of
         *  logical cores that are implemented using a multithreading type approach.
         *  This value is:
         *      1   Performance of PEs at the lowest affinity level is very interdependent.
         *          Affinity0 represents threads. Cortex-A75 is not multithreaded,
         *          but may be in a system with other cores that are multithreaded.
         */
        public final long MT;

        /** Affinity level 2. Second highest level affinity field.
         *      CLUSTERID   Indicates the value read in the CLUSTERIDAFF2 configuration signal.
         */
        public final long Aff2;

        /** Part of Affinity level 1. Third highest level affinity field.
         *      [15:12] RAZ     Read-As-Zero.
         *      [11: 8] CPUID	Identification number for each CPU in the Cortex-A75 cluster:
         *                      0x0 - 0x7	MP1:CPUID:0 to MP8:CPUID:7
         */
        public final long Aff1;

        /** Affinity level 0. The level identifies individual threads within a multithreaded core.
         *  The Cortex-A75 core is single-threaded, so this field has the value 0x00.
         */
        public final long Aff0;

        // returns 0x80000000
        MPIDR_EL1(){
            VALUE = getAA64MRS("MPIDR_EL1",0);
            //[63:40] Reserved, RES0.---------
            Aff3  = (int)((VALUE >> 32) & 0xFF);
            //[31] Reserved, RES1.---------
            U     = (int)((VALUE >> 30) & 0x1);
            //[29:25] Reserved, UNK---------
            MT    = (int)((VALUE >> 24) & 0x1);
            Aff2  = (int)((VALUE >> 16) & 0xFF);
            Aff1  = (int)((VALUE >>  8) & 0xFF);
            Aff0  = (int)(VALUE & 0xFF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put(".value",VALUE);
                o.put("Aff0", Aff0);
                o.put("Aff1",Aff1);
                o.put("Aff2", Aff2);
                o.put("MT",MT);
                o.put("U", U);
                o.put("Aff3",Aff3);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** Revision ID Register, EL1
     *  Provides revision information, additional to MIDR_EL1,
     *  that identifies minor fixes (errata) which might be present
     *  in a specific implementation of the Cortex-A75 core.
     *  https://developer.arm.com/documentation/100403/0200/register-descriptions/aarch64-system-registers/revidr-el1--revision-id-register--el1
     */
    public static class REVIDR_EL1 {

        private static REVIDR_EL1 sClass;

        public static REVIDR_EL1 getInstance(){
            if (sClass == null)
                sClass = new REVIDR_EL1();
            return sClass;
        }

        public final long VALUE;


        REVIDR_EL1(){
            VALUE = getAA64MRS("REVIDR_EL1",0);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put(".value",VALUE);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }
    }

    /** Main ID Register, EL1
     *  Provides identification information for the core,
     *  including an implementer code for the device and a device ID number.
     */
    public static class MIDR_EL1 {

        private static MIDR_EL1 sClass;

        public static MIDR_EL1 getInstance(){
            if (sClass == null)
                sClass = new MIDR_EL1();
            return sClass;
        }

        public static MIDR_EL1 read(long v){
            return new MIDR_EL1(v);
        }

        public final long VALUE;

        /** Indicates the implementer code.
         */
        public final int Implementer;

        /** Indicates the variant number of the core.
         *  This is the major revision number x in the rx part of
         *  the rxpy description of the product revision status.
         */
        public final int Variant;

        /** Indicates the architecture code.
         */
        public final int Architecture;

        /** Indicates the primary part number.
         *
         */
        public final int PartNum;

        /** Indicates the minor revision number of the core.
         *  This is the minor revision number y in the py part
         *  of the rxpy description of the product revision status.
         */
        public final int Revision;

        private String strImplementer, strPartNum;

        MIDR_EL1(){
            this(getAA64MRS("MIDR_EL1",0));
        }

        MIDR_EL1(long value){
            VALUE        = value;
            Implementer  = (int)((VALUE >> 24) & 0xFF);
            Variant      = (int)((VALUE >> 20) & 0xF);
            Architecture = (int)((VALUE >> 16) & 0xF);
            PartNum      = (int)((VALUE >>  4) & 0xFFF);
            Revision     = (int)(VALUE & 0xF);
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject o = new JSONObject();
            try {
                o.put(".value",VALUE);
                o.put("Implementer",Implementer);
                o.put("Variant",Variant);
                o.put("Architecture",Architecture);
                o.put("PartNum",PartNum);
                o.put("Revision",Revision);
                return o.toString();
            } catch (JSONException e){
                if (DBG) Log.e(TAG, e.toString());
            }
            return super.toString();
        }

        public String getImplementerName(){
            if (strImplementer == null)
                strPartNum = setStrNames();
            return strImplementer;
        }

        public String getPartNumName(){
            if (strPartNum == null)
                strPartNum = setStrNames();
            return strPartNum;
        }

        private String setStrNames(){
            switch (Implementer) {
                case 0x41: // 'A': ARM
                    strImplementer = "Arm Limited";
                    switch (PartNum) {
                        case 0xb02: return "arm_mpcore";
                        case 0xb36: return "arm_1136jf_s";
                        case 0xb56: return "arm_1156t2f_s";
                        case 0xb76: return "arm_1176jzf_s";
                        case 0xc05: return "arm_cortex_a5";
                        case 0xc07: return "arm_cortex_a7";
                        case 0xc08: return "arm_cortex_a8";
                        case 0xc09: return "arm_cortex_a9";
                        case 0xc0d: return "arm_cortex_a12";
                        case 0xc0f: return "arm_cortex_a15";
                        case 0xc0e: return "arm_cortex_a17";
                        case 0xc14: return "arm_cortex_r4";
                        case 0xc15: return "arm_cortex_r5";
                        case 0xc17: return "arm_cortex_r7";
                        case 0xc18: return "arm_cortex_r8";
                        case 0xc20: return "arm_cortex_m0";
                        case 0xc21: return "arm_cortex_m1";
                        case 0xc23: return "arm_cortex_m3";
                        case 0xc24: return "arm_cortex_m4";
                        case 0xc27: return "arm_cortex_m7";
                        case 0xd01: return "arm_cortex_a32";
                        case 0xd02: return "arm_cortex_a34";
                        case 0xd03: return "arm_cortex_a53";
                        case 0xd04: return "arm_cortex_a35";
                        case 0xd05: return "arm_cortex_a55";
                        case 0xd06: return "arm_cortex_a65";
                        case 0xd07: return "arm_cortex_a57";
                        case 0xd08: return "arm_cortex_a72";
                        case 0xd09: return "arm_cortex_a73";
                        case 0xd0a: return "arm_cortex_a75";
                        case 0xd0b: return "arm_cortex_a76";
                        case 0xd0c: return "arm_neoverse_n1";
                        case 0xd0d: return "arm_cortex_a77";
                        case 0xd0e: return "arm_cortex_a76ae";
                        case 0xd13: return "arm_cortex_r52";
                        case 0xd20: return "arm_cortex_m23";
                        case 0xd21: return "arm_cortex_m33";
                        case 0xd22: return "arm_cortex_m55";
                        case 0xd40: return "arm_neoverse_v1";
                        case 0xd41: return "arm_cortex_a78";
                        case 0xd43: return "arm_cortex_a65ae";
                        case 0xd44: return "arm_cortex_x1";
                        case 0xd49: return "arm_neoverse_n2";
                        case 0xd4a: return "arm_neoverse_e1";
                        default: break;
                    }
                case 0x42: // 'B': Broadcom Corp.
                    strImplementer = "Broadcom Corporation";
                    switch (PartNum) {
                        case 0x00f: return "broadcom_brahma_b15";
                        case 0x100: return "broadcom_brahma_b53";
                        case 0x516: return "cavium_thunderx2t99p1";
                        default: break;
                    }
                case 0x43: // 'C': Cavium
                    strImplementer = "Cavium Inc.";
                    switch (PartNum) {
                        case 0xa0: return "cavium_thunderx";
                        case 0xa1: return Variant == 0 ? "cavium_thunderx88p1" : "cavium_thunderx88";
                        case 0xa2: return "cavium_thunderx81";
                        case 0xa3: return "cavium_thunderx83";
                        case 0xaf: return "cavium_thunderx2t99";
                        case 0xb0: return "cavium_octeontx2";
                        case 0xb1: return "cavium_octeontx2t98";
                        case 0xb2: return "cavium_octeontx2t96";
                        case 0xb3: return "cavium_octeontx2f95";
                        case 0xb4: return "cavium_octeontx2f95n";
                        case 0xb5: return "cavium_octeontx2f95mm";
                        case 0xb8: return "marvell_thunderx3t110";
                        default: break;
                    }
                case 0x44: //
                    strImplementer = "Digital Equipment Corporation";
                    break;
                case 0x46: // 'F': Fujitsu
                    strImplementer = "Fujitsu Ltd.";
                    if (PartNum == 0x1) {
                        return "fujitsu_a64fx";
                    }
                    break;
                case 0x48: // 'H': HiSilicon
                    switch (PartNum) {
                        case 0xd01: return "hisilicon_tsv110";
                        case 0xd40: return "arm_cortex_a76 (Kirin 980)";
                        default: break;
                    }
                case 0x49: //
                    strImplementer = "Infineon Technologies AG";
                    break;
                case 0x4d:
                    strImplementer = "Motorola or Freescale Semiconductor Inc.";
                    break;
                case 0x4e: // 'N': NVIDIA
                    strImplementer = "NVIDIA Corporation";
                    switch (PartNum) {
                        case 0x000: return "nvidia_denver1";
                        case 0x003: return "nvidia_denver2";
                        case 0x004: return "nvidia_carmel";
                        default: break;
                    }
                case 0x50: // 'P': AppliedMicro
                    strImplementer = "Applied Micro Circuits Corporation";
                    // x-gene 2
                    // x-gene 3
                    if (PartNum == 0x000) {
                        return "apm_xgene1";
                    }
                    break;
                case 0x51: // 'Q': Qualcomm
                    strImplementer = "Qualcomm Inc.";
                    switch (PartNum) {
                        case 0x00f:
                        case 0x02d:
                            return "qualcomm_scorpion";
                        case 0x04d:
                        case 0x06f:
                            return "qualcomm_krait";
                        case 0x201: case 0x211: return "kyro silver";
                        case 0x205: return "kyro gold";
                        case 0x800: return "kyro 2xx arm_cortex_a73 gold";
                        case 0x801: return "kyro 2xx arm_cortex_a53 silver";
                        case 0x802: return "kyro 3xx arm_cortex_a75 gold";
                        case 0x803: return "kyro 3xx arm_cortex_a55 silver";
                        case 0x804: return "kyro 4xx arm_cortex_a76 gold";
                        case 0x805: return "kyro 4xx arm_cortex_a55 silver";
                        //case 0x8..: return "kyro 5xx arm_cortex_a77 gold";
                        //case 0x8..: return "kyro 5xx arm_cortex_a55 silver";
                        case 0xc00:
                            return "qualcomm_falkor";
                        case 0xc01:
                            return "qualcomm_saphira";
                        default: break;
                    }
                case 0x53: // 'S': Samsung
                    strImplementer = "Samsung Electronics Co., Ltd.";
                    if (PartNum != 1 && Variant != 1) break;
                    switch (PartNum) {
                        case 0x1: return Variant == 4 ? "samsung_exynos_m2" : "samsung_exynos_m1";
                        case 0x2: return "samsung_exynos_m3";
                        case 0x3: return "samsung_exynos_m4";
                        case 0x4: return "samsung_exynos_m5";
                        default: break;
                    }
                case 0x56: // 'V': Marvell
                    strImplementer = "Marvell International Ltd.";
                    switch (PartNum) {
                        case 0x581:
                        case 0x584:
                            return "marvell_pj4";
                        default: break;
                    }
                case 0x67: // 'a': Apple
                    strImplementer = "Apple Inc.";
                    // ref: https://opensource.apple.com/source/xnu/xnu-6153.81.5/osfmk/arm/cpuid.h.auto.html
                    switch (PartNum) {
                        case 0x0: return "apple_swift";
                        case 0x1: return "apple_a7 cyclone";
                        case 0x2: return "apple_a8 typhoo";
                        case 0x3: return "apple_a8 typhoo/capri";
                        case 0x4: return "apple_a9 twister";
                        case 0x5: return "apple_a9 twister/elba/malta";
                        case 0x6: return "apple_a10 hurricane";
                        case 0x7: return "apple_a10 hurricane/myst";
                        case 0x8: return "apple_a11 monsoon";
                        case 0x9: return "apple_a11 mistral";
                        case 0xB: return "apple_a12 vortex";
                        case 0xC: return "apple_a12 tempest";
                        case 0x10: return "apple_a12 A12X, vortex aruba";
                        case 0x11: return "apple_a12 A12X, tempest aruba";
                        case 0xF: return "apple_s4 tempest M9";
                        case 0x12: return "apple_a13 lightning";
                        case 0x13: return "apple_a13 thunder";
                        default: break;
                    }
                case 0x68: // 'h': Huaxintong Semiconductor
                    strImplementer = "Huaxintong Semiconductor Technologies Co., Ltd.";
                    if (PartNum == 0x0) {
                        return "hxt_phecda";
                    }
                    break;
                case 0x69: // Intel
                    strImplementer = "Intel Corporation";
                    if (PartNum == 0x001) {
                        return "intel_3735d";
                    }
                default: break;
            }
            return String.format("Generic (0x%x)",PartNum);
        }
    }
}
