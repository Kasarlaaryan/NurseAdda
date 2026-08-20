import React from 'react';
import { Card, CardContent } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Mail, Phone, MapPin, Send, MessageSquare, Twitter, Linkedin, Facebook } from 'lucide-react';
import { useToast } from '../../components/common/Toast';

export const ContactPage: React.FC = () => {
  const { showToast } = useToast();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    showToast('success', 'Message Sent', 'Thank you for reaching out. Our team will contact you shortly.');
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 pt-20 px-4 pb-24">
      <div className="max-w-7xl mx-auto">
        <div className="mb-20 text-center max-w-3xl mx-auto">
          <span className="px-4 py-1.5 rounded-full bg-sky-50 dark:bg-sky-500/10 text-sky-600 text-[10px] font-black uppercase tracking-widest border border-sky-100 dark:border-sky-500/20">
            Get In Touch
          </span>
          <h1 className="mt-8 text-5xl font-black text-slate-900 dark:text-white tracking-tight">
            How can we help?
          </h1>
          <p className="mt-6 text-lg text-slate-500 font-medium leading-relaxed">
            Have questions about our platform? Our specialized healthcare support team is here to assist you.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-12 mb-24">
          <div className="lg:col-span-2">
            <Card className="border-none shadow-none bg-white dark:bg-slate-900 rounded-[32px] p-2">
              <CardContent className="p-8 md:p-12">
                <form onSubmit={handleSubmit} className="space-y-8">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="space-y-3">
                      <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 px-1">Full Name</label>
                      <input 
                        type="text" 
                        required
                        className="w-full h-14 bg-slate-50 dark:bg-slate-950 border border-slate-100 dark:border-slate-800 rounded-2xl px-6 font-bold text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-sky-500/20"
                        placeholder="John Doe"
                      />
                    </div>
                    <div className="space-y-3">
                      <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 px-1">Email Address</label>
                      <input 
                        type="email" 
                        required
                        className="w-full h-14 bg-slate-50 dark:bg-slate-950 border border-slate-100 dark:border-slate-800 rounded-2xl px-6 font-bold text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-sky-500/20"
                        placeholder="john@example.com"
                      />
                    </div>
                  </div>
                  
                  <div className="space-y-3">
                    <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 px-1">Subject</label>
                    <select className="w-full h-14 bg-slate-50 dark:bg-slate-950 border border-slate-100 dark:border-slate-800 rounded-2xl px-6 font-bold text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-sky-500/20 appearance-none">
                      <option>General Inquiry</option>
                      <option>Hospital Sales</option>
                      <option>Agency Partnership</option>
                      <option>Technical Support</option>
                    </select>
                  </div>

                  <div className="space-y-3">
                    <label className="text-[10px] font-black uppercase tracking-widest text-slate-400 px-1">Message</label>
                    <textarea 
                      required
                      rows={6}
                      className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-100 dark:border-slate-800 rounded-2xl p-6 font-bold text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-sky-500/20"
                      placeholder="Tell us how we can help..."
                    ></textarea>
                  </div>

                  <Button type="submit" size="lg" className="h-14 px-12 text-lg font-black shadow-xl shadow-sky-500/20" rightIcon={<Send className="w-5 h-5" />}>
                    Send Message
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div>

          <div className="space-y-8">
            <div className="p-10 rounded-[48px] bg-sky-600 text-white shadow-2xl shadow-sky-500/20">
              <h3 className="text-2xl font-black mb-8">Contact Info</h3>
              <div className="space-y-10">
                <div className="flex gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center flex-shrink-0">
                    <Mail className="w-6 h-6" />
                  </div>
                  <div>
                    <p className="text-[10px] font-black uppercase tracking-widest text-sky-200 mb-1">Email Us</p>
                    <p className="font-bold text-lg">support@nurseadda.com</p>
                  </div>
                </div>

                <div className="flex gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center flex-shrink-0">
                    <Phone className="w-6 h-6" />
                  </div>
                  <div>
                    <p className="text-[10px] font-black uppercase tracking-widest text-sky-200 mb-1">Call Us</p>
                    <p className="font-bold text-lg">+91 (800) 123-4567</p>
                  </div>
                </div>

                <div className="flex gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center flex-shrink-0">
                    <MapPin className="w-6 h-6" />
                  </div>
                  <div>
                    <p className="text-[10px] font-black uppercase tracking-widest text-sky-200 mb-1">Visit Us</p>
                    <p className="font-bold text-lg leading-relaxed">Cyber Park, Block A-402,<br />Gurugram, HR 122003</p>
                  </div>
                </div>
              </div>

              <div className="pt-12 mt-12 border-t border-white/10 flex gap-6">
                <button className="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center hover:bg-white/20 transition-colors">
                  <Twitter className="w-5 h-5" />
                </button>
                <button className="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center hover:bg-white/20 transition-colors">
                  <Linkedin className="w-5 h-5" />
                </button>
                <button className="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center hover:bg-white/20 transition-colors">
                  <Facebook className="w-5 h-5" />
                </button>
              </div>
            </div>

            <Card className="border-none shadow-none bg-white dark:bg-slate-900 rounded-[32px]">
              <CardContent className="p-10 text-center">
                <div className="w-16 h-16 rounded-2xl bg-emerald-50 dark:bg-emerald-500/10 flex items-center justify-center text-emerald-600 mx-auto mb-6">
                  <MessageSquare className="w-8 h-8" />
                </div>
                <h4 className="text-xl font-black text-slate-900 dark:text-white mb-4">Live Support</h4>
                <p className="text-sm text-slate-500 font-medium leading-relaxed mb-8">
                  Our live chat support is available Monday to Saturday, 9am - 6pm IST.
                </p>
                <Button variant="outline" className="w-full h-12 rounded-2xl font-black uppercase tracking-widest text-[10px]">
                  Start Live Chat
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};
