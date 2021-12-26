classdef RMTF5_2 < PROBLEM
% <multi/many> <real> <large/none>
% The neural network training problem
% nHidden1 --- 10 --- Size of the first hidden layer
% nHidden2 --- 5 --- Size of the second hidden layer

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

% The datasets are taken from the UCI machine learning repository in
% http://archive.ics.uci.edu/ml/index.php
% Name                              Samples Features Classes
% Statlog_German                     1000      24       2

    properties(Access = private)
        TrainIn;    % Input of training set
        TrainOut;   % Output of training set
        TrainLabel; % Output labels of training set
        TestIn;   	% Input of test set
        TestOut;  	% Output of test set
        TestLabel;  % Output labels of test set
        nHidden1;    % Size of the first hidden layer
		nHidden2;    % Size of the second hidden layer
    end
    methods
        %% Default settings of the problem
        function Setting(obj)
            [obj.nHidden1,obj.nHidden2] = obj.ParameterSet(10,5);
			% Load data
            Data  = load('Dataset\SpeakerAccentFrES.txt');
            % Data preprocessing: Standardization
			Mean  = mean(Data(:,1:end-1),1);
            Std   = std(Data(:,1:end-1),[],1);
            Input = (Data(:,1:end-1)-repmat(Mean,size(Data,1),1))./repmat(Std,size(Data,1),1);
            Output = Data(:,end);
            obj.TrainIn  = Input(1:ceil(end*0.8),:);
            obj.TrainOut = Output(1:ceil(end*0.8),:);
            obj.TestIn   = Input(ceil(end*0.8)+1:end,:);
            obj.TestOut  = Output(ceil(end*0.8)+1:end,:);
            obj.TrainLabel = obj.TrainOut;
            obj.TestLabel  = obj.TestOut;
            % Parameter setting
            obj.M        = 2;
            obj.D        = (size(obj.TrainIn,2)+1)*obj.nHidden1 + (obj.nHidden1+1)*obj.nHidden2 + (obj.nHidden2+1)*1;
            obj.lower    = zeros(1,obj.D) - 1;
            obj.upper    = zeros(1,obj.D) + 1;
            obj.encoding = 'real';
        end
        %% Calculate objective values
        function PopObj = CalObj(obj,PopDec)
            PopObj = zeros(size(PopDec,1),2);
            for i = 1 : size(PopDec,1)
			    star = 1;
				ter = (size(obj.TrainIn,2)+1)*obj.nHidden1;
                W1 = reshape(PopDec(i,star:ter),size(obj.TrainIn,2)+1,obj.nHidden1);
				star = ter+1;
				ter = ter + (obj.nHidden1+1)*obj.nHidden2;
				W2 = reshape(PopDec(i,star:ter),obj.nHidden1+1,obj.nHidden2);
				star = ter+1;
                W3 = reshape(PopDec(i,star:end),obj.nHidden2+1,1);
				Z = Predict(obj.TrainIn,W1,W2,W3);%DecayingSineUnit activation
                %L1 regularization term
				PopObj(i,1) = mean(abs(PopDec(i,:)));
				%Mean Absolute Error
                PopObj(i,2) =  mean(abs(Z-obj.TrainLabel));
            end
        end
        %% Display a population in the objective space
        function DrawObj(obj,Population)
            Draw(Population.objs,{'Complexity of neural network','Training error',[]});
        end
    end
end

function [Z,Y1,Y2] = Predict(X,W1,W2,W3)
   out1 = [ones(size(X,1),1),X]*W1;
    L1 = (out1-pi)./pi;
	L2 = (out1+pi)./pi;
	Y1 = 0.5*pi*(sinc(L1)-sinc(L2));%DecayingSineUnit: 0.5*PI*[sinc(x-pi)-sinc(x+pi)]
	out2 = [ones(size(Y1,1),1),Y1]*W2;
	L3 = (out2-pi)./pi;
	L4 = (out2+pi)./pi;
    Y2 = 0.5*pi*(sinc(L3)-sinc(L4));%DecayingSineUnit
    Z = 1./(1+exp(-[ones(size(Y2,1),1),Y2]*W3));%Sigmoid
end