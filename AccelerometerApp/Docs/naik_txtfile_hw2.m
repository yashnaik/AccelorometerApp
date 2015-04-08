%Created by Yash Naik ECE573
%Usage :naik_txtfile_hw2('filename.txt')
% The script accepts a text file with three numeric columns
% and plot the behavior of every colum element against time
function f=naik_txtfile_hw2(file)
array=dlmread(file,' ',2,0);

x=array(:,2);
y=array(:,8);
z=array(:,14);

len=length(x);

time = 50:50:(50*len);
p=plot(time,x,'g',time,y,'b',time,z,'r');
p(1).LineWidth=3;
p(2).LineWidth=2;
p(3).LineWidth=4;
xlabel('time in milliseconds')
ylabel('Value of Acceleration')


