'use client';

import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

interface PieChartData {
  name: string;
  value: number;
  color?: string;
}

interface CustomPieChartProps {
  data: PieChartData[];
  title?: string;
}

const COLORS = ['#60B5FF', '#FF9149', '#FF9898', '#FF90BB', '#FF6363', '#80D8C3', '#A19AD3', '#72BF78'];

export function CustomPieChart({ data, title }: CustomPieChartProps) {
  const chartData = data?.map?.((item, index) => ({
    ...item,
    color: item?.color ?? COLORS[index % COLORS.length],
  })) ?? [];

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value ?? 0);
  };

  return (
    <div className="w-full h-full">
      {title && <h3 className="text-lg font-semibold mb-4 text-center">{title}</h3>}
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={chartData}
            cx="50%"
            cy="50%"
            labelLine={false}
            outerRadius={80}
            fill="#8884d8"
            dataKey="value"
          >
            {chartData?.map?.((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry?.color ?? '#8884d8'} />
            )) ?? null}
          </Pie>
          <Tooltip 
            formatter={(value: any) => formatCurrency(Number(value ?? 0))}
            contentStyle={{ fontSize: 11 }}
          />
          <Legend 
            verticalAlign="top" 
            align="center"
            wrapperStyle={{ fontSize: 11 }}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
