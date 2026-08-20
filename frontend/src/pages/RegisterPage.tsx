import React from 'react';
import { Link } from 'react-router-dom';
import {
  Stethoscope,
  Building2,
  ChevronRight,
  ShieldCheck,
} from 'lucide-react';

export const RegisterPage: React.FC = () => {
  return (
    <div className="max-w-md mx-auto space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="text-center space-y-1">
        <h2 className="text-2xl font-black text-white tracking-tight">Select Your Journey</h2>
        <p className="text-xs text-slate-400">Choose how you want to use the NurseAdda platform</p>
      </div>

      <div className="grid grid-cols-1 gap-4">
        <Link
          to="/register-staff"
          className="group relative p-5 rounded-2xl border border-slate-800 bg-slate-800/40 text-left transition-all hover:border-sky-500 hover:bg-sky-500/5"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-sky-500/10 text-sky-500 flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
              <Stethoscope className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <h3 className="text-sm font-black text-slate-100 group-hover:text-sky-400 transition-colors">
                Healthcare Professional
              </h3>
              <p className="text-[11px] text-slate-500 mt-0.5">I am a Nurse, ANM, or GNM looking for shifts.</p>
            </div>
            <ChevronRight className="w-4 h-4 text-slate-700 group-hover:text-sky-500" />
          </div>
        </Link>

        <Link
          to="/register-client"
          className="group relative p-5 rounded-2xl border border-slate-800 bg-slate-800/40 text-left transition-all hover:border-emerald-500 hover:bg-emerald-500/5"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
              <Building2 className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <h3 className="text-sm font-black text-slate-100 group-hover:text-emerald-400 transition-colors">
                Facility / Hospital Partner
              </h3>
              <p className="text-[11px] text-slate-500 mt-0.5">
                I represent a hospital looking for verified staff.
              </p>
            </div>
            <ChevronRight className="w-4 h-4 text-slate-700 group-hover:text-emerald-500" />
          </div>
        </Link>
      </div>

      <div className="text-center text-xs text-slate-500">
        Already have an account?{' '}
        <Link to="/login" className="text-sky-400 font-bold hover:underline">
          Log In
        </Link>
      </div>

      <div className="pt-2 border-t border-slate-800/80 text-center text-[10px] text-slate-500 flex items-center justify-center gap-1.5 uppercase tracking-widest font-bold">
        <ShieldCheck className="w-4 h-4 text-emerald-500" />
        <span>Verified Credentialing Standards</span>
      </div>
    </div>
  );
};
