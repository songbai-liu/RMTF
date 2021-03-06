classdef RMTF7_2 < PROBLEM
% <multi/many> <real> <large/none>
% The portfolio optimization problem

%------------------------------- Reference --------------------------------
% S. Liu, Q. Lin, L. Feng, K. C. Wong, and K. C. Tan, Evolutionary Multitasking
% for Large-Scale Multiobjective Optimization, IEEE
% Transactions on Evolutionary Computation, 2022.
%------------------------------- Copyright --------------------------------
% Copyright (c) 2021 BIMK Group. You are free to use the PlatEMO for
% research purposes. All publications which use this platform or any code
% in the platform should acknowledge the use of "PlatEMO" and reference "Ye
% Tian, Ran Cheng, Xingyi Zhang, and Yaochu Jin, PlatEMO: A MATLAB platform
% for evolutionary multi-objective optimization [educational forum], IEEE
% Computational Intelligence Magazine, 2017, 12(4): 73-87".
%--------------------------------------------------------------------------

% The datasets are the minutely closing prices of EUR/CHF taken from MT4
% Name	    Instruments   Length
% PoData2	   5000         50

    properties(Access = private)
        Yield;
        Risk;
    end
    methods
        %% Default settings of the problem
        function Setting(obj)
			% Load data
            Data  = load('Dataset\PoData2.txt');
            obj.Yield = log(Data(:,2:end)) - log(Data(:,1:end-1));
            obj.Risk  = cov(obj.Yield');
            % Parameter setting
            obj.M = 2;
            obj.D = size(obj.Yield,1);
            obj.lower    = zeros(1,obj.D) - 1;
            obj.upper    = zeros(1,obj.D) + 1;
            obj.encoding = 'real';
        end
        %% Calculate objective values
        function PopObj = CalObj(obj,PopDec)
            PopDec = PopDec./repmat(max(sum(abs(PopDec),2),1),1,size(PopDec,2));            
            PopObj = zeros(size(PopDec,1),2);
            for i = 1 : size(PopDec,1)
                PopObj(i,1) = PopDec(i,:)*obj.Risk*PopDec(i,:)';
                PopObj(i,2) = 1 - sum(PopDec(i,:)*obj.Yield);
            end
        end
        %% Display a population in the objective space
        function DrawObj(obj,Population)
            PopObj = Population.objs;
            Draw([PopObj(:,1),1-PopObj(:,2)],{'Risk','Return',[]});
        end
    end
end