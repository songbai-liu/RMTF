# RMTF:a new multitasking real-world large-scale multiobjective optimization problem test suite
The RMTF suite includes seven different multitask optimization problems (i.e., RMTF1 to RMTF7), 
in which each RMTF problem has 2 or 3 practical tasks and each task is a 2-objective real-world 
LMOP. Concretely, RMTF1-RMTF6 are formulated to simulate the training of DNNs on multiple different 
binary classification tasks, where each task endeavors to optimize the weights (i.e., the variables) 
of the involved DNN for concurrently minimizing its complexity and classification error. Here, the considered DNN has only 
two hidden layers and two parameters (h1 and h2) are used to control the neurons in these two 
hidden layers, respectively. Since each neuron needs to specify an activation function, five 
different activation functions (A1-A5) are used in RMTF1-6 problems. Besides, three cost 
functions, i.e., the MSE, the mean absolute error (MAE), and the root mean squared error (RMSE), 
are included to evaluate the classification error. Moreover, the widely used two types of regularization 
(i.e., L1 and L2 regularizations) are adopted to reflect the complexity of the DNN. Finally, 
fourteen different datasets (D1 to D14) from various fields are included in RMTF1-6 to train 
the DNN for bi-classification. In summary, RMTF1-RMTF6 are formulated by considering from different 
deployments involved in these DNN-based classification tasks, including the training dataset, 
the error function, the activation function, the regularization, and the structure of the DNN. 
Regarding the RMTF7, it is defined as a 2-task portfolio optimization problem on different datasets, 
where each task is a 2-objective LMOP that aims to find the portfolio of instruments having the 
largest expected return and the lowest risk.

# DRNEA: An evolutionary algorithm via discriminative reconstruction network (DRN).
Evolutionary transfer optimization (ETO) has been becoming a hot research topic in the field of 
evolutionary computation, which is based on the fact that knowledge learning and transfer across 
the related optimization exercises can improve the effi-ciency of others. However, rare studies 
employ ETO to solve large-scale multiobjective optimization problems (LMOPs). To fill this 
research gap, this paper proposes a new multitasking ETO algorithm via a powerful transfer 
learning model to simultaneously solve multiple LMOPs. In particular, inspired by adversarial 
domain adaptation in transfer learning, a discriminative reconstruction network (DRN) model 
(containing an encoder, a decoder, and a classifier) is created for each LMOP. At each generation, 
the DRN is trained by the currently obtained nondominated solutions for all LMOPs via backpropagation 
with gradient descent. With this well-trained DRN model, the proposed algorithm can: 
(1) transfer the solutions of source LMOPs directly to the target LMOP for assisting its optimization, 
(2) evaluate the correlation between the source and target LMOPs to control the transfer of solutions, 
(3) learn a dimensional-reduced Pareto-optimal subspace of the target LMOP to improve the efficiency 
of transfer optimization in the large-scale search space.
