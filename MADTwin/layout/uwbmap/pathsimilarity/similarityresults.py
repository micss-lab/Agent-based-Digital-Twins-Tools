import csv

import matplotlib.pyplot as plt
import numpy as np
import similaritymeasures
from PyQt6.QtWidgets import QTableWidget, QTableWidgetItem, QMainWindow

# define the table as dictionary
result_table = {}


class ResultTable(QMainWindow):
    def display_table(self):
        # reset the table by setting the row count to 0
        self.table_widget_similarity_measure.setRowCount(0)
        self.table_widget_similarity_measure: QTableWidget
        self.table_widget_similarity_measure.setColumnCount(2)
        self.table_widget_similarity_measure.setHorizontalHeaderLabels(["Similarity Measure Name", "Result"])
        if len(result_table) > 0:
            for key, value in result_table.items():
                rows = self.table_widget_similarity_measure.rowCount()
                self.table_widget_similarity_measure.resizeColumnsToContents()
                self.table_widget_similarity_measure.setRowCount(rows + 1)
                self.table_widget_similarity_measure.setItem(rows, 0, QTableWidgetItem(str(key)))
                self.table_widget_similarity_measure.setItem(rows, 1, QTableWidgetItem(str(value)))

        else:
            print("The Result List is Empty")

    def calculate_similarity(self, file1, file2):
        print("Calculate Similarity", self)
        # physical_agent_file = 'C:/temp/PhysicalAgentCoordinates_6a75.csv'
        # digital_agent_file = 'C:/temp/DualAgentCoordinates_PhysicalAgent_112_dual@Main-Platform.csv'

        physical_agent_file = file1
        digital_agent_file = file2

        file_one_x = list()
        file_one_y = list()
        file_two_x = list()
        file_two_y = list()

        with open(physical_agent_file, 'r') as file:
            csvreader = csv.reader(file)
            for column in csvreader:
                try:
                    # add the values in the first and the second column in the CSV file
                    # add the x coordinate from the first file
                    file_one_x.append(int(column[1]))
                    # add the y coordinate from the first file
                    file_one_y.append(int(column[2]))
                except Exception as ex:
                    print('The variable is not a number')

        with open(digital_agent_file, 'r') as file:
            csvreader = csv.reader(file)
            for column in csvreader:
                try:
                    # add the values in the first and the second column in the CSV file
                    # add the x coordinate from the second file
                    file_two_x.append(int(column[1]))
                    # add the y coordinate from the second file
                    file_two_y.append(int(column[2]))
                except Exception as ex:
                    print('The variable is not a number')

        # get the least length of the two lists, so we can compare the elements in the both lists
        if len(file_one_x) < len(file_two_x):
            data_len = len(file_one_x)
        else:
            data_len = len(file_two_x)

        physical_agent_data = np.zeros((len(file_one_x), 2))
        print("Physical Agent List: ", len(physical_agent_data))
        physical_agent_data[:, 0] = file_one_x
        physical_agent_data[:, 1] = file_one_y
        physical_agent_plot_data = physical_agent_data
        # make the length of the list equal to the digital agent list
        physical_agent_data = physical_agent_data[:data_len]

        digital_agent_data = np.zeros((len(file_two_x), 2))
        print("Digital Agent List: ", len(digital_agent_data))
        digital_agent_data[:, 0] = file_two_x
        digital_agent_data[:, 1] = file_two_y
        digital_agent_plot_data = digital_agent_data
        # make the length of the list equal to the physical agent list
        digital_agent_data = digital_agent_data[:data_len]

        # quantify the difference between the two curves using PCM
        pcm = similaritymeasures.pcm(physical_agent_data, digital_agent_data)
        result_table["Partial Curve Mapping (PCM)"] = pcm

        # quantify the difference between the two curves using
        # Discrete Frechet distance
        fd = similaritymeasures.frechet_dist(physical_agent_data, digital_agent_data)
        result_table["Fréchet Distance"] = fd

        # quantify the difference between the two curves using
        # area between two curves
        area = similaritymeasures.area_between_two_curves(physical_agent_data, digital_agent_data)
        result_table["Area Between Two Curves"] = area

        # quantify the difference between the two curves using
        # Curve Length based similarity measure
        cl = similaritymeasures.curve_length_measure(physical_agent_data, digital_agent_data)
        result_table["Distance Length Between Two Curves"] = cl

        # quantify the difference between the two curves using
        # Dynamic Time Warping distance
        dtw, d = similaritymeasures.dtw(physical_agent_data, digital_agent_data)
        result_table["Dynamic Time Warping (DTW)"] = dtw

        # mean absolute error
        mae = similaritymeasures.mae(physical_agent_data, digital_agent_data)
        result_table["Mean Absolute Error (MAE)"] = mae

        # mean squared error
        mse = similaritymeasures.mse(physical_agent_data, digital_agent_data)
        result_table["Mean Squared Error (MAE)"] = mae

        # print the results
        # print("==================================")
        # print('Fréchet Distance: ', fd)
        # print('Partial Curve Mapping (PCM): ', pcm)
        # print('Mean Squared Error (MAE): ', mse)
        # print('Area Between Two Curves: ', area)
        # print('Curve Lengths: ', cl)
        # print('Dynamic Time Warping (DTW): ', dtw)
        # print('Mean Absolute Error (MAE): ', mae)
        # print("==================================")

        # plot the data
        plt.figure(figsize=(8, 6))
        plt.title("Similarity Measurement Between the Two Coordinate Paths", fontsize=12, ha="center")
        plt.plot(physical_agent_plot_data[:, 0], physical_agent_plot_data[:, 1], 'r', linewidth=1.5, label="Physical Agent Path")
        plt.plot(digital_agent_plot_data[:, 0], digital_agent_plot_data[:, 1], linewidth=1.5, linestyle='--', label="Digital Agent Path")
        plt.ylabel("Y: Coordinates", fontsize=10)
        plt.xlabel("X: Coordinates", fontsize=10)
        plt.legend(loc="best")
        plt.show()


