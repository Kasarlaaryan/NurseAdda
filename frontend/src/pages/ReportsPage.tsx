import React from 'react';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { useToast } from '../components/common/Toast';
import { BarChart3, TrendingUp, Users, DollarSign, Download, Calendar } from 'lucide-react';

export const ReportsPage: React.FC = () => {
  const { showToast } = useToast();
  
  return (
    <div className="space-y-6">
      <PageHeader
        title="Reports & Workforce Analytics"
        description="Comprehensive intelligence on shift fulfillment rates, agency revenue, overtime compliance & staff performance."
        actions={
          <Button 
            variant="outline" 
            size="sm" 
            leftIcon={<Download className="w-4 h-4" />}
            onClick={() => showToast('success', 'Report Exported', 'Executive analytics CSV has been generated and download started.')}
          >
            Export Executive Report (CSV)
          </Button>
        }
      />

      {/* Analytics Summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-slate-400">Shift Match Rate</span>
              <BadgeClass text="98.4%" />
            </div>
            <p className="text-3xl font-black text-slate-900 dark:text-slate-100">98.4%</p>
            <p className="text-xs text-emerald-600 font-semibold">+2.1% higher than benchmark</p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-slate-400">Total Hours Billed</span>
              <BadgeClass text="1,420 Hours" />
            </div>
            <p className="text-3xl font-black text-slate-900 dark:text-slate-100">1,420 Hours</p>
            <p className="text-xs text-teal-600 font-semibold">Across 28 Partner Hospitals</p>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-slate-400">Average Fulfillment Time</span>
              <BadgeClass text="18 Minutes" />
            </div>
            <p className="text-3xl font-black text-slate-900 dark:text-slate-100">18 Mins</p>
            <p className="text-xs text-purple-600 font-semibold">Emergency Request Response</p>
          </CardContent>
        </Card>
      </div>

      {/* Detailed Report Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Staffing Category Distribution</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold">
                <span>Registered Nurses (RN / ICU)</span>
                <span>54% (76 Staff)</span>
              </div>
              <div className="w-full bg-slate-100 dark:bg-slate-800 rounded-full h-2.5">
                <div className="bg-teal-600 h-2.5 rounded-full w-[54%]" />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold">
                <span>Caregivers & CNAs</span>
                <span>24% (34 Staff)</span>
              </div>
              <div className="w-full bg-slate-100 dark:bg-slate-800 rounded-full h-2.5">
                <div className="bg-purple-600 h-2.5 rounded-full w-[24%]" />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex justify-between text-xs font-bold">
                <span>Physiotherapists & Techs</span>
                <span>22% (32 Staff)</span>
              </div>
              <div className="w-full bg-slate-100 dark:bg-slate-800 rounded-full h-2.5">
                <div className="bg-blue-600 h-2.5 rounded-full w-[22%]" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Client Hospital Utilization</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-xs">
            <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 flex justify-between items-center">
              <div>
                <p className="font-bold text-slate-900 dark:text-slate-100">City General Hospital</p>
                <p className="text-slate-500">22 Active Staff Allocated</p>
              </div>
              <span className="font-extrabold text-teal-600">$142,500 Billed</span>
            </div>

            <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 flex justify-between items-center">
              <div>
                <p className="font-bold text-slate-900 dark:text-slate-100">Apex Care Clinic Group</p>
                <p className="text-slate-500">8 Active Staff Allocated</p>
              </div>
              <span className="font-extrabold text-teal-600">$68,400 Billed</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

const BadgeClass = ({ text }: { text: string }) => (
  <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-teal-100 dark:bg-teal-950 text-teal-800 dark:text-teal-300">
    {text}
  </span>
);
