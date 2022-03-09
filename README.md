# RMTF:a new multitasking real-world large-scale multiobjective optimization problem test suite
The RMTF suite includes seven different multitask optimiza-tion problems (i.e., RMTF1 to RMTF7), 
in which each RMTF problem has 2 or 3 practical tasks and each task is a 2-objective real-world 
LMOP. Concretely, RMTF1-RMTF6 are formulated to simulate the training of DNNs on multiple different 
bi-classification tasks, where each task endeavors to optimize the weights (i.e., the variables) 
of the involved DNN for concurrently minimizing its complexity and classi-fication error (i.e., 
the two objectives as defined in the supplementary document). Here, the considered DNN has only 
two hidden layers and two parameters (h1 and h2) are used to control the neurons in these two 
hidden layers, respectively. Since each neuron needs to specify an activation function, five 
different activa-tion functions (A1-A5) are used in RMTF1-6 problems. Be-sides, three cost 
functions, i.e., the MSE, the mean absolute error (MAE), and the root mean squared error (RMSE), 
are included to evaluate the classification error. Moreover, the widely used two types of regularization 
(i.e., L1 and L2 regu-larizations) are adopted to reflect the complexity of the DNN. Finally, 
fourteen different datasets (D1 to D14) from various fields are included in RMTF1-6 to train 
the DNN for bi-classification. In summary, RMTF1-RMTF6 are formu-lated by considering from different 
deployments involved in these DNN-based classification tasks, including the training dataset, 
the error function, the activation function, the regularization, and the structure of the DNN. 
Regarding the RMTF7, it is defined as a 2-task portfolio optimization problem on different datasets, 
where each task is a 2-objective LMOP that aims to find the portfo-lio of instruments having the 
largest expected return and the lowest risk.
