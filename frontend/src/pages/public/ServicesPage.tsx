import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/common/Card';
import { ShieldCheck, Users, Activity, Clock, ChevronRight, CheckCircle2, Layout, Database, Terminal, PhoneCall } from 'lucide-react';
import { Button } from '../../components/common/Button';

export const ServicesPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 pt-20 px-4 pb-24">
      <div className="max-w-7xl mx-auto">
        <div className="mb-20 text-center max-w-3xl mx-auto">
          <span className="px-4 py-1.5 rounded-full bg-amber-50 dark:bg-amber-500/10 text-amber-600 text-[10px] font-black uppercase tracking-widest border border-amber-100 dark:border-amber-500/20">
            Our Solutions
          </span>
          <h1 className="mt-8 text-5xl font-black text-slate-900 dark:text-white tracking-tight">
            Comprehensive Staffing <br />
            <span className="text-amber-600">Enterprise Solutions</span>
          </h1>
          <p className="mt-6 text-lg text-slate-500 font-medium leading-relaxed">
            NurseAdda WFM provides a complete ecosystem for hospitals, staffing agencies, and healthcare professionals to collaborate seamlessly.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-24">
          {[
            {
              title: 'Hospital Management',
              desc: 'For healthcare facilities to manage staffing requests, budgets, and compliance across all departments.',
              icon: Layout,
              color: 'sky'
            },
            {
              title: 'Agency Operations',
              desc: 'Empower your staffing agency with automated dispatch, credentialing, and real-time assignment tracking.',
              icon: Database,
              color: 'indigo'
            },
            {
              title: 'Professional Portal',
              desc: 'Nurses and staff get a dedicated mobile-first interface to manage shifts, credentials, and payments.',
              icon: Terminal,
              color: 'emerald'
            },
            {
              title: 'Compliance & Verification',
              desc: '256-bit AES secured document storage with automated Aadhaar and Nursing Council integration.',
              icon: ShieldCheck,
              color: 'amber'
            },
            {
              title: 'Smart Billing Engine',
              desc: 'Automated invoice generation based on geo-fenced attendance and pre-negotiated facility rates.',
              icon: CheckCircle2,
              color: 'rose'
            },
            {
              title: 'Dedicated Support',
              desc: '24/7 technical and operational support for all enterprise partners in our staffing ecosystem.',
              icon: PhoneCall,
              color: 'violet'
            }
          ].map((service, i) => (
            <Card key={i} className="group hover:border-amber-500/50 transition-all border-none bg-white dark:bg-slate-900 overflow-hidden shadow-none">
              <CardContent className="p-8">
                <div className={`w-14 h-14 rounded-2xl bg-${service.color}-50 dark:bg-${service.color}-500/10 flex items-center justify-center text-${service.color}-600 mb-8 group-hover:scale-110 transition-transform`}>
                  <service.icon className="w-7 h-7" />
                </div>
                <h3 className="text-xl font-black text-slate-900 dark:text-white mb-4">{service.title}</h3>
                <p className="text-slate-500 text-sm leading-relaxed font-medium mb-8">{service.desc}</p>
                <Button variant="ghost" size="sm" className="text-amber-600 font-black tracking-widest uppercase text-[10px] p-0 h-auto hover:bg-transparent" rightIcon={<ChevronRight className="w-3 h-3" />}>
                  Learn More
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
          <div className="p-12 rounded-[48px] bg-slate-900 text-white relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-64 h-64 bg-amber-600/10 rounded-full blur-3xl -mr-32 -mt-32" />
            <div className="relative z-10">
              <h2 className="text-3xl font-black mb-6">For Hospitals</h2>
              <ul className="space-y-4 mb-10">
                {[
                  'Reduce staffing shortages with instant broadcasting',
                  'Ensuring 100% credential compliance',
                  'Simplify billing with automated invoicing',
                  'Analyze performance with executive reports'
                ].map((item, i) => (
                  <li key={i} className="flex items-center gap-3 text-slate-300 font-medium text-sm">
                    <CheckCircle2 className="w-5 h-5 text-amber-500 flex-shrink-0" />
                    {item}
                  </li>
                ))}
              </ul>
              <Button size="lg" className="h-14 px-10 text-lg font-black bg-amber-600 hover:bg-amber-700 shadow-xl shadow-amber-900/20">
                Contact Sales
              </Button>
            </div>
          </div>
          <div className="p-12 rounded-[48px] bg-amber-600 text-white relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl -mr-32 -mt-32" />
            <div className="relative z-10">
              <h2 className="text-3xl font-black mb-6">For Staffing Agencies</h2>
              <ul className="space-y-4 mb-10">
                {[
                  'Automated workforce dispatching system',
                  'Manage thousands of credentials securely',
                  'Real-time attendance and assignment tracking',
                  'Faster payment processing and reporting'
                ].map((item, i) => (
                  <li key={i} className="flex items-center gap-3 text-amber-50 font-medium text-sm">
                    <CheckCircle2 className="w-5 h-5 text-white flex-shrink-0" />
                    {item}
                  </li>
                ))}
              </ul>
              <Button size="lg" className="h-14 px-10 text-lg font-black bg-white text-amber-600 hover:bg-slate-50 shadow-xl shadow-amber-900/20">
                Partner with us
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
