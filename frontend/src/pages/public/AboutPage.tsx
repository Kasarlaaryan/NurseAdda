import React from 'react';
import { PageHeader } from '../../components/layouts/PageHeader';
import { Card, CardContent } from '../../components/common/Card';
import { Activity, ShieldCheck, Heart, Award, Users, Target } from 'lucide-react';

export const AboutPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 pt-20">
      <div className="max-w-7xl mx-auto px-4 pb-20">
        <div className="mb-16 text-center max-w-3xl mx-auto">
          <span className="px-4 py-1.5 rounded-full bg-amber-50 dark:bg-amber-500/10 text-amber-600 text-[10px] font-black uppercase tracking-widest border border-amber-100 dark:border-amber-500/20">
            Our Mission
          </span>
          <h1 className="mt-6 text-4xl md:text-5xl font-black text-slate-900 dark:text-white tracking-tight">
            Revolutionizing Healthcare Staffing
          </h1>
          <p className="mt-6 text-lg text-slate-500 font-medium leading-relaxed">
            NurseAdda WFM was founded with a singular purpose: to bridge the gap between healthcare facilities and qualified professionals through technology.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center mb-24">
          <div className="space-y-6">
            <h2 className="text-3xl font-black text-slate-900 dark:text-white">Our Story</h2>
            <p className="text-slate-500 font-medium leading-relaxed">
              In 2024, we recognized that the traditional healthcare staffing model was fragmented and inefficient. Hospitals struggled to find qualified nurses, while nurses lacked a transparent platform to find opportunities.
            </p>
            <p className="text-slate-500 font-medium leading-relaxed">
              NurseAdda WFM was built to solve this by creating an intelligent workforce management system that automates the entire process from staffing requests to secure billing.
            </p>
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800">
                <p className="text-2xl font-black text-amber-600">10k+</p>
                <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Professionals</p>
              </div>
              <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800">
                <p className="text-2xl font-black text-emerald-600">50+</p>
                <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Hospitals</p>
              </div>
            </div>
          </div>
          <div className="aspect-square bg-white dark:bg-slate-900 rounded-[48px] border-8 border-slate-100 dark:border-slate-800 shadow-2xl flex items-center justify-center p-12">
            <Activity className="w-32 h-32 text-amber-600/20" />
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-24">
          {[
            {
              title: 'Integrity First',
              desc: 'We maintain the highest standards of professional credentialing and verification.',
              icon: ShieldCheck,
              color: 'sky'
            },
            {
              title: 'Empowerment',
              desc: 'Giving nurses and healthcare staff the tools to manage their own professional growth.',
              icon: Heart,
              color: 'rose'
            },
            {
              title: 'Innovation',
              desc: 'Continuously refining our algorithms to ensure the right person for every shift.',
              icon: Target,
              color: 'indigo'
            }
          ].map((item, i) => (
            <Card key={i} className="border-none shadow-none bg-white dark:bg-slate-900">
              <CardContent className="pt-8">
                <div className={`w-14 h-14 rounded-2xl bg-${item.color}-50 dark:bg-${item.color}-500/10 flex items-center justify-center text-${item.color}-600 mb-6`}>
                  <item.icon className="w-7 h-7" />
                </div>
                <h3 className="text-xl font-black text-slate-900 dark:text-white mb-4">{item.title}</h3>
                <p className="text-slate-500 text-sm leading-relaxed font-medium">{item.desc}</p>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="p-12 rounded-[48px] bg-amber-600 text-white text-center">
          <h2 className="text-3xl font-black mb-6">Ready to join our network?</h2>
          <p className="text-amber-100 font-medium mb-10 max-w-xl mx-auto">
            Whether you're a hospital looking for staff or a nurse looking for assignments, we have the tools you need.
          </p>
          <div className="flex flex-col md:flex-row items-center justify-center gap-4">
            <button className="h-14 px-10 bg-white text-amber-600 rounded-2xl font-black text-lg shadow-xl shadow-amber-900/20">
              Register Now
            </button>
            <button className="h-14 px-10 bg-amber-700 text-white rounded-2xl font-bold text-lg border border-amber-500">
              Contact Sales
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
