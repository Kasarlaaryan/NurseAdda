import React from 'react';
import { 
  Shield, 
  ExternalLink, 
  Mail, 
  Phone, 
  Twitter, 
  Linkedin, 
  Github,
  CheckCircle2,
  Lock
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';

export const Footer: React.FC = () => {
  const { activeRole } = useAuthStore();

  const getPlatformLinks = () => {
    switch (activeRole) {
      case 'ROLE_STAFF':
        return [
          { label: 'My Schedule', href: '/assignments' },
          { label: 'Shift Clock-In', href: '/attendance' },
          { label: 'Earnings & Invoices', href: '/invoices' },
          { label: 'My Profile', href: '/profile' },
        ];
      case 'ROLE_USER':
        return [
          { label: 'Ongoing Staffing', href: '/assignments' },
          { label: 'Post New Request', href: '/requests' },
          { label: 'Organization Billing', href: '/invoices' },
          { label: 'Facility Settings', href: '/profile' },
        ];
      case 'ROLE_SUPER_ADMIN':
      case 'ROLE_ADMIN':
      default:
        return [
          { label: 'Staff Verifications', href: '/verifications' },
          { label: 'Assignment Ops', href: '/assignments' },
          { label: 'System Analytics', href: '/' },
          { label: 'Global Settings', href: '/settings' },
        ];
    }
  };

  const platformLinks = getPlatformLinks();

  return (
    <footer className="mt-20 border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12 md:py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12 lg:gap-8">
          {/* Brand Column */}
          <div className="space-y-6">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-xl bg-amber-600 text-white font-black text-sm flex items-center justify-center shadow-lg shadow-amber-600/20">
                N
              </div>
              <span className="font-black text-lg tracking-tight text-slate-900 dark:text-white">
                NurseAdda<span className="text-amber-500">WFM</span>
              </span>
            </div>
            <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed max-w-xs">
              The next-generation Workforce Management platform designed specifically for modern healthcare staffing and shift optimization.
            </p>
            <div className="flex items-center gap-4">
              <a href="#" className="p-2 rounded-lg bg-slate-50 dark:bg-slate-900 text-slate-400 hover:text-amber-500 transition-colors border border-slate-100 dark:border-slate-800">
                <Twitter className="w-4 h-4" />
              </a>
              <a href="#" className="p-2 rounded-lg bg-slate-50 dark:bg-slate-900 text-slate-400 hover:text-amber-500 transition-colors border border-slate-100 dark:border-slate-800">
                <Linkedin className="w-4 h-4" />
              </a>
              <a href="#" className="p-2 rounded-lg bg-slate-50 dark:bg-slate-900 text-slate-400 hover:text-amber-500 transition-colors border border-slate-100 dark:border-slate-800">
                <Github className="w-4 h-4" />
              </a>
            </div>
          </div>

          {/* Platform Links */}
          <div className="space-y-6">
            <h4 className="text-xs font-black uppercase tracking-widest text-slate-900 dark:text-white">Platform</h4>
            <ul className="space-y-3">
              {platformLinks.map((link) => (
                <li key={link.label}>
                  <Link to={link.href} className="text-sm text-slate-500 hover:text-amber-500 transition-colors flex items-center gap-2">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact & Compliance */}
          <div className="space-y-6">
            <h4 className="text-xs font-black uppercase tracking-widest text-slate-900 dark:text-white">Contact Us</h4>
            <ul className="space-y-4">
              <li className="flex items-start gap-3">
                <div className="p-1.5 rounded-lg bg-amber-500/10 text-amber-500 mt-0.5">
                  <Mail className="w-3.5 h-3.5" />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">Support Email</p>
                  <p className="text-sm font-medium text-slate-700 dark:text-slate-300">support@nurseadda.com</p>
                </div>
              </li>
              <li className="flex items-start gap-3">
                <div className="p-1.5 rounded-lg bg-amber-500/10 text-amber-500 mt-0.5">
                  <Phone className="w-3.5 h-3.5" />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">Emergency Desk</p>
                  <p className="text-sm font-medium text-slate-700 dark:text-slate-300">+1 (800) NURSE-HQ</p>
                </div>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="mt-16 pt-8 border-t border-slate-100 dark:border-slate-800 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex flex-col md:flex-row items-center gap-4 md:gap-8">
            <span className="text-xs text-slate-500">© {new Date().getFullYear()} NurseAdda Inc. All rights reserved.</span>
          </div>
          
          <div className="flex items-center gap-4 px-4 py-2 rounded-full bg-slate-50 dark:bg-slate-900 border border-slate-100 dark:border-slate-800">
            <div className="flex items-center gap-2">
              <Lock className="w-3 h-3 text-emerald-500" />
              <span className="text-[10px] font-black uppercase tracking-widest text-slate-500">HIPAA Compliant</span>
            </div>
            <div className="w-px h-3 bg-slate-200 dark:bg-slate-800" />
            <div className="flex items-center gap-2">
              <Shield className="w-3 h-3 text-amber-500" />
              <span className="text-[10px] font-black uppercase tracking-widest text-slate-500">SSL Encrypted</span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};
