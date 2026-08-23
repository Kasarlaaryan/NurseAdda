import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/common/Button';
import { ShieldCheck, Users, Activity, Clock, ChevronRight, CheckCircle2 } from 'lucide-react';
import { motion } from 'motion/react';
import { useAuthStore } from '../../store/useAuthStore';

export const LandingPage: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      {/* Hero Section */}
      <section className="pt-32 pb-20 px-4">
        <div className="max-w-7xl mx-auto text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <span className="px-4 py-1.5 rounded-full bg-amber-50 dark:bg-amber-500/10 text-amber-600 text-[10px] font-black uppercase tracking-widest border border-amber-100 dark:border-amber-500/20">
              Next-Gen Healthcare Staffing
            </span>
            <h1 className="mt-8 text-5xl md:text-7xl font-black text-slate-900 dark:text-white tracking-tight leading-[1.1]">
              The Workforce Engine for <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-600 to-indigo-600">
                Modern Healthcare
              </span>
            </h1>
            <p className="mt-8 text-lg text-slate-500 max-w-2xl mx-auto font-medium leading-relaxed">
              NurseAdda WFM automates staffing requests, manages credentialing, and simplifies billing for hospitals and staffing agencies across India.
            </p>
            <div className="mt-12 flex flex-col md:flex-row items-center justify-center gap-4">
              <Link to="/register">
                <Button size="lg" className="h-14 px-10 text-lg font-black shadow-xl shadow-amber-500/25" rightIcon={<ChevronRight className="w-5 h-5" />}>
                  Join the Network
                </Button>
              </Link>
              <Link to="/about">
                <Button variant="outline" size="lg" className="h-14 px-10 text-lg font-bold border-2">
                  Learn More
                </Button>
              </Link>
            </div>
          </motion.div>

          <motion.div 
            className="mt-20 relative max-w-5xl mx-auto"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.2, duration: 0.5 }}
          >
            <div className="aspect-video bg-white dark:bg-slate-900 rounded-[32px] border border-slate-200 dark:border-slate-800 shadow-2xl overflow-hidden p-4">
               <div className="w-full h-full bg-slate-50 dark:bg-slate-950 rounded-2xl border border-slate-100 dark:border-slate-900 flex items-center justify-center">
                  <Activity className="w-20 h-20 text-slate-200 dark:text-slate-800 animate-pulse" />
               </div>
            </div>
            <div className="absolute -bottom-6 -right-6 w-64 p-6 bg-white dark:bg-slate-900 rounded-3xl shadow-xl border border-slate-200 dark:border-slate-800 hidden md:block">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-500/10 flex items-center justify-center text-emerald-600">
                  <ShieldCheck className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-xs font-black text-slate-900 dark:text-white">Verified Pool</p>
                  <p className="text-[10px] text-emerald-600 font-bold">10,000+ Nurses</p>
                </div>
              </div>
              <div className="space-y-2">
                <div className="h-2 w-full bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                  <div className="h-full w-4/5 bg-emerald-500" />
                </div>
                <p className="text-[10px] text-slate-400 text-right font-bold uppercase tracking-widest">94% Fulfillment</p>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="py-24 px-4 bg-white dark:bg-slate-950">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-black text-slate-900 dark:text-white">Everything your facility needs</h2>
            <p className="mt-4 text-slate-500 font-medium">Enterprise-grade tools for scaling your healthcare workforce.</p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              {
                title: 'Real-time Deployment',
                description: 'Broadcast shift requirements to thousands of verified professionals instantly.',
                icon: Clock,
                color: 'sky'
              },
              {
                title: 'Compliance Vault',
                description: 'Automated Aadhar and Nursing Council verification with secure cloud storage.',
                icon: ShieldCheck,
                color: 'emerald'
              },
              {
                title: 'Smart Billing',
                description: 'Automated invoicing based on verified attendance and agreed hospital rates.',
                icon: CheckCircle2,
                color: 'indigo'
              }
            ].map((feature, i) => (
              <div key={i} className="p-8 rounded-[32px] bg-slate-50 dark:bg-slate-900 border border-slate-100 dark:border-slate-800 hover:border-amber-500/50 transition-all group">
                <div className={`w-14 h-14 rounded-2xl bg-${feature.color}-50 dark:bg-${feature.color}-500/10 flex items-center justify-center text-${feature.color}-600 mb-6 group-hover:scale-110 transition-transform`}>
                  <feature.icon className="w-7 h-7" />
                </div>
                <h3 className="text-xl font-black text-slate-900 dark:text-white mb-4">{feature.title}</h3>
                <p className="text-slate-500 text-sm leading-relaxed font-medium">
                  {feature.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-20 px-4 bg-slate-50 dark:bg-slate-950 border-t border-slate-200 dark:border-slate-800">
        <div className="max-w-7xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-12">
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-2 mb-6">
              <Activity className="w-6 h-6 text-amber-600" />
              <span className="text-xl font-black tracking-tighter">NurseAdda</span>
            </div>
            <p className="text-sm text-slate-500 font-medium leading-relaxed">
              Empowering healthcare facilities with efficient workforce management since 2024.
            </p>
          </div>
          
          <div>
            <h4 className="font-black text-xs uppercase tracking-[0.2em] text-slate-900 dark:text-white mb-6">Platform</h4>
            <ul className="space-y-4 text-sm text-slate-500 font-bold">
              <li><Link to="/services" className="hover:text-amber-600">Services</Link></li>
              <li><Link to="/register" className="hover:text-amber-600">Register</Link></li>
              <li><Link to="/login" className="hover:text-amber-600">Sign In</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="font-black text-xs uppercase tracking-[0.2em] text-slate-900 dark:text-white mb-6">Company</h4>
            <ul className="space-y-4 text-sm text-slate-500 font-bold">
              <li><Link to="/about" className="hover:text-amber-600">About Us</Link></li>
              <li><Link to="/contact" className="hover:text-amber-600">Contact</Link></li>
              <li><Link to="/privacy" className="hover:text-amber-600">Privacy Policy</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="font-black text-xs uppercase tracking-[0.2em] text-slate-900 dark:text-white mb-6">Support</h4>
            <ul className="space-y-4 text-sm text-slate-500 font-bold">
              <li><a href="#" className="hover:text-amber-600">Help Center</a></li>
              <li><a href="#" className="hover:text-amber-600">API Docs</a></li>
              <li><a href="#" className="hover:text-amber-600">Status</a></li>
            </ul>
          </div>
        </div>
        
        <div className="max-w-7xl mx-auto pt-12 mt-12 border-t border-slate-200 dark:border-slate-800 flex flex-col md:flex-row items-center justify-between gap-6">
          <p className="text-xs text-slate-400 font-bold">© 2024 NurseAdda WFM. All rights reserved.</p>
          <div className="flex items-center gap-6">
            <ShieldCheck className="w-5 h-5 text-slate-300" />
            <span className="text-[10px] text-slate-400 font-black uppercase tracking-widest">ISO 27001 Certified</span>
          </div>
        </div>
      </footer>
    </div>
  );
};
