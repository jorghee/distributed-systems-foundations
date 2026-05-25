import React from 'react';

const Card: React.FC<{ title: string; children: React.ReactNode; className?: string }> = ({ title, children, className }) => (
    <div className={`bg-gray-800 rounded-lg p-6 shadow-lg ${className}`}>
        <h2 className="text-2xl font-semibold mb-4 text-purple-300">{title}</h2>
        {children}
    </div>
);

export default Card;

